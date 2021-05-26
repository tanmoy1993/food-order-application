using DeliveryService.Configuration;
using DeliveryService.Misc;
using DeliveryService.Models;
using Microsoft.Extensions.Logging;
using MongoDB.Driver;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.ExceptionServices;
using System.Threading.Tasks;

namespace DeliveryService.Services
{
    public class DeliveryContextService
    {
        private readonly IMongoCollection<DeliveryContext> _dlv_ctx;
        private readonly ILogger<DeliveryContextService> _logger;
        public DeliveryContextService(ILogger<DeliveryContextService> logger, IDeliveryServiceDBConfig settings)
        {
            _logger = logger;

            var client = new MongoClient(settings.ConnectionString);
            var database = client.GetDatabase(settings.DatabaseName);

            _dlv_ctx = database.GetCollection<DeliveryContext>(settings.DeliveryContextCollectionName);
        }

        public async Task<DeliveryContext> GetAsync(string id)
        {
            FindOptions<DeliveryContext> options = new FindOptions<DeliveryContext> { Limit = 1 };
            IAsyncCursor<DeliveryContext> task = await _dlv_ctx.FindAsync(x => x.OrderId.Equals(id), options);
            List<DeliveryContext> list = await task.ToListAsync();
            DeliveryContext ctx = null;
            try
            {
                ctx = list.First();
            }
            catch (InvalidOperationException e)
            {
                _logger.LogError($"Invalid operation for get delivery: {e.Message}");
                throw new DeliveryServiceException("Invalid delivery id.");
            }
            return ctx;
        }

        public async Task<List<DeliveryContext>> GetByRestaurantAsync(string id)
        {
            FindOptions<DeliveryContext> options = new FindOptions<DeliveryContext> { Limit = 100 };
            IAsyncCursor<DeliveryContext> task = await _dlv_ctx.FindAsync(x => x.RestaurantId.Equals(id), options);
            List<DeliveryContext> listCtx = await task.ToListAsync();
            
            return listCtx;
        }

        public async Task<bool> Create(OrderReceived rec)
        {
            DeliveryContext ctx = new DeliveryContext(rec);
            ctx.ExpireOn = DateTime.UtcNow.AddMinutes(30);
            await _dlv_ctx.InsertOneAsync(ctx);

            return true;
        }

        private async Task<bool> UpdateAsync(string id, UpdateDefinition<DeliveryContext> updates)
        {
            var filter = Builders<DeliveryContext>.Filter.Eq(ctx => ctx.OrderId, id);
            var result = await _dlv_ctx.UpdateOneAsync(filter, updates);
            return result.IsAcknowledged;
        }

        private async Task OrderCompletedCheck(string id)
        {
            DeliveryContext ctx = await GetAsync(id);
            if (ctx.CurrentStatus >= DeliveryContext.Status.PAYMENT_FAIL)
            {
                _logger.LogError($"Cancelled/delivered order update check for id:{id} with {ctx.CurrentStatus}");
                throw new DeliveryServiceException($"Order id:{id} has been cancelled/delivered already.");
            }

        }

        //update status through web endpoint calls
        public async Task<bool> UpdateStatus(DeliveryContext ctx)
        {

            _logger.LogInformation($"Confirmed order for id:{ctx.OrderId}");
            await OrderCompletedCheck(ctx.OrderId);
            var updates = Builders<DeliveryContext>.Update
                                                .Set(c => c.CurrentStatus, ctx.CurrentStatus);

            if (ctx.CurrentStatus == DeliveryContext.Status.CONFIRMED)
            {
                if (ctx.DeliveryETAMins <= 5)
                    throw new DeliveryServiceException("Invalid delivery estimate.");
                updates.Set(c => c.DeliveryETAMins, ctx.DeliveryETAMins)
                        .Set(c => c.ExpireOn, DateTime.UtcNow.AddMinutes(30));
            }
            else //for cancel by restaurant or delivered
                updates.Set(c => c.ExpireOn, DateTime.UtcNow.AddMinutes(5));

            Task<bool> result = UpdateAsync(ctx.OrderId, updates);

            switch (ctx.CurrentStatus) {
                case DeliveryContext.Status.CONFIRMED:
                    GenericProducer<OrderConfirmed>.dlvCtxQueue.Enqueue(ctx);
                    break;
                case DeliveryContext.Status.RESTAURANT_CANCELLED:
                    GenericProducer<OrderCancelled>.dlvCtxQueue.Enqueue(ctx);
                    break;
                case DeliveryContext.Status.DELIVERED:
                    GenericProducer<OrderDelivered>.dlvCtxQueue.Enqueue(ctx);
                    break;
                default:
                    throw new DeliveryServiceException("Invalid delivery status.");
            }

            return await result;
            
        }

        public async Task<bool> UpdateStatusAndRemove(string id, DeliveryContext.Status status)
        {
            try
            {
                _logger.LogInformation($"Cancelled order for id:{id}");
                await OrderCompletedCheck(id);
                var updates = Builders<DeliveryContext>.Update.Set(c => c.CurrentStatus, status)
                                                        .Set(c => c.ExpireOn, DateTime.UtcNow.AddMinutes(5));
                return await UpdateAsync(id, updates);
            }
            catch (DeliveryServiceException e)
            {
                _logger.LogError($"Exception during status update: {e.Message}");
                return false;
            }

        }

        public async Task<bool> UpdateContactDetails(BuyerContact contact)
        {
            try
            {
                _logger.LogInformation($"Update contact for id:{contact.orderId}");
                await OrderCompletedCheck(contact.orderId);
                var updates = Builders<DeliveryContext>.Update.Set(c => c.contact, contact);
                return await UpdateAsync(contact.orderId, updates);
            }
            catch (DeliveryServiceException e)
            {
                _logger.LogError($"Exception during contact update: {e.Message}");
                return false;
            }

        }

    }
}
