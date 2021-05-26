import requests
import json
import random
import time

from kafka import KafkaProducer
from kafka import KafkaConsumer
from kafka.errors import KafkaError

headers = {'Content-Type': 'application/json'}
baseUrl = "http://0.0.0.0:8202/order/"
sleep_time = 3

producer = KafkaProducer(bootstrap_servers=['172.17.0.1:9092'])
producer = KafkaProducer(
    value_serializer=lambda m: json.dumps(m).encode('ascii'))

consumerOrderReceived = KafkaConsumer('fo_ord_rec', auto_offset_reset = 'earliest',
                         group_id='my-group1',
                         bootstrap_servers=['localhost:9092'],
                         value_deserializer=lambda m: json.loads(m.decode('ascii')))

consumerOrderCancelled = KafkaConsumer('fo_ord_can', auto_offset_reset = 'earliest',
                         group_id='my-group2',
                         bootstrap_servers=['localhost:9092'],
                         value_deserializer=lambda m: json.loads(m.decode('ascii')))


def get_rand_long():
    return str(random.randint(10000, 99999))

def get_order_ctx():
    return {
        "buyerId": "abcd" + get_rand_long(),
        "restaurantId": "asdf" + get_rand_long(),
        "totalCost": 100,
        "orderedItems": [
            {
                "foodId": "asdad" + get_rand_long(),
                "name": "asdad" + get_rand_long(),
                "cost": 25,
                "qty": 2
            },
            {
                "foodId": "qw22e" + get_rand_long(),
                "name": "asdad" + get_rand_long(),
                "cost": 55,
                "qty": 1
            }
        ]
    }

def create_order(errors=None):
    print("creating order")    
    body = get_order_ctx()
    res = requests.post(baseUrl, headers=headers, json=body)
    print(res.status_code)
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    if(errors):
        assert res.status_code == 400
        for e in errors:
            assert e in response_body["message"]
        return

    assert res.status_code == 200
    assert "orderId" in response_body
    assert body["buyerId"] == response_body["buyerId"]
    assert body["restaurantId"] == response_body["restaurantId"]
    assert len(body["orderedItems"]) == len(response_body["orderedItems"])
    time.sleep(sleep_time)
    return response_body

def track_order(order_ctx, expected_status, expected_expire=-1, errors=None):
    print("tracking order")
    res = requests.get(baseUrl + "track/" + order_ctx["orderId"], headers=headers)
    print(res.status_code)
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    if(errors):
        assert res.status_code == 400
        for e in errors:
            assert e in response_body["message"]
        return

    assert res.status_code == 200
    assert "orderId" in response_body
    assert order_ctx["buyerId"] == response_body["buyerId"]
    assert order_ctx["restaurantId"] == response_body["restaurantId"]
    assert len(order_ctx["orderedItems"]) == len(response_body["orderedItems"])

    assert expected_status == response_body["currentStatus"]
    return response_body

def cancel_order(order_ctx, errors=None):
    print("cancel order")
    res = requests.put(baseUrl + "cancel", headers=headers, json=order_ctx)
    print(res.status_code)
    if(errors):
        assert res.status_code == 400
        response_body = json.loads(res.text)
        print(json.dumps(response_body, indent=4))
        for e in errors:
            assert e in response_body["message"]
        return

    assert res.status_code == 200
    return

def create_order_invalid(errors):
    print("creating order")    
    body = {
        "restaurantId": "asdf" + get_rand_long(),
        "totalCost": 100,
        "orderedItems": [
            {
                "foodId": "asdad" + get_rand_long(),
                "name": "asdad" + get_rand_long(),
                "cost": 25,
                "qty": 2
            },
            {
                "name": "asdad" + get_rand_long(),
                "cost": 55,
                "qty": 1
            }
        ]
    }
    res = requests.post(baseUrl, headers=headers, json=body)
    print(res.status_code)
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    assert res.status_code == 400
    message = str(response_body["errors"])
    for e in errors:
        assert e in message
    
    return 

def assert_latest_topic(topic, orderId, error=False):

    consumer = None
    if topic == "rec":
        consumer = consumerOrderReceived
    else:
        consumer = consumerOrderCancelled
    
    time.sleep(sleep_time)
    print("consuming topic: " + topic)
    orderIdPresent = False
    msg_pack = consumer.poll(timeout_ms=1000) #consume all message from beginning
    max_offset_partition = [-1, -1]
    max_offset_messages_partition = [{"orderId": ""}, {"orderId": ""}]
    for tp, messages in msg_pack.items():
        for message in messages:
            print ("%s:%d:%d: key=%s value=%s" % (tp.topic, tp.partition,
                                                message.offset, message.key,
                                                message.value))
            for header in message.headers:
                print("%s " % (str(header)))
            if(max_offset_partition[tp.partition] < message.offset):
                max_offset_messages_partition[tp.partition] = message.value
                max_offset_partition[tp.partition] = message.offset
    
    # each topic is triggered at most once for a orderid
    orderIdPresent = (orderId == max_offset_messages_partition[0]["orderId"]) ^ \
                            (orderId == max_offset_messages_partition[1]["orderId"])

    print("consumed topic: " + topic)
    if error:
        assert ~orderIdPresent
    else:
        assert orderIdPresent
    return

def message_adapter_cnf(order_ctx, topic_context=None):
    topic_name = "fo_ord_cnf"
    message_body = {"orderId" : order_ctx["orderId"], \
                "buyerId" : order_ctx["buyerId"], \
                "deliveryETA" : 20}
    return [topic_name, message_body]

def message_adapter_can(order_ctx, topic_context=None):
    if topic_context==None:
        topic_context="RESTAURANT_CANCELLED"
    topic_name = "fo_ord_can"
    message_body = {"orderId" : order_ctx["orderId"], \
                "buyerId" : order_ctx["buyerId"], \
                "restaurantId" : order_ctx["restaurantId"], \
                "reason" : topic_context}
    return [topic_name, message_body]

def message_adapter_pay(order_ctx, topic_context=True):
    if topic_context==None:
        topic_context=True
    topic_name = "fo_pay_sta"
    message_body = {"orderId" : order_ctx["orderId"], \
                "status" : topic_context, \
                "message" : "dummy"}
    return [topic_name, message_body]


message_adapters = {"cnf" : message_adapter_cnf, \
                    "can" : message_adapter_can, \
                    "pay" : message_adapter_pay}
def send_message(topic, order_ctx, topic_context=None):

    message_adapter = message_adapters[topic]
    [topic_name, message_body] = message_adapter(order_ctx, topic_context)
    future = producer.send(topic_name, message_body)

    try:
        record_metadata = future.get(timeout=10)
    except KafkaError:
        log.exception()
        pass

    print (record_metadata.topic)
    print (record_metadata.partition)
    print (record_metadata.offset)
    time.sleep(sleep_time)
    return

if __name__=="__main__":

    # create order
    order_ctx = create_order()
    # track -> RECEIVED
    track_order(order_ctx, "RECEIVED")
    # ord_rec latest assert
    assert_latest_topic("rec", order_ctx["orderId"])
    # send ord cnf
    send_message("cnf", order_ctx)
    # track -> CONFIRMED
    track_order(order_ctx, "CONFIRMED")
    # send payment success
    send_message("pay", order_ctx)
    # track -> PAYMENT_SUCCESS
    track_order(order_ctx, "PAYMENT_SUCCESS")
    # cancel order
    cancel_order(order_ctx)
    # ord_can latest assert
    assert_latest_topic("can", order_ctx["orderId"])
    # track order -> status and expire
    track_order(order_ctx, "USER_CANCELLED")
    print("completed scenario 1")

    # create order
    order_ctx = create_order()
    # track -> RECEIVED
    track_order(order_ctx, "RECEIVED")
    # send payment success
    send_message("pay", order_ctx)
    # track -> PAYMENT_SUCCESS
    track_order(order_ctx, "PAYMENT_SUCCESS")
    # send ord cnf
    send_message("cnf", order_ctx)
    # track -> CONFIRMED
    track_order(order_ctx, "CONFIRMED")
    # cancel order
    cancel_order(order_ctx)
    # ord_can latest assert
    assert_latest_topic("can", order_ctx["orderId"])
    # track order -> status and expire
    track_order(order_ctx, "USER_CANCELLED")
    print("completed scenario 2")

    # create order
    order_ctx = create_order()
    # track -> RECEIVED
    track_order(order_ctx, "RECEIVED")
    # send payment success
    send_message("pay", order_ctx, False)
    # track -> PAYMENT_FAIL
    track_order(order_ctx, "PAYMENT_FAIL")
    # send ord cnf
    send_message("cnf", order_ctx)
    # track -> PAYMENT_FAIL
    track_order(order_ctx, "PAYMENT_FAIL")
    # cancel order
    cancel_order(order_ctx, [""])
    # ord_can latest assert no trigger
    assert_latest_topic("can", order_ctx["orderId"], True)
    # track order -> status(PAYMENT_FAIL) and expire
    track_order(order_ctx, "PAYMENT_FAIL")
    print("completed scenario 3")

    # create order
    order_ctx = create_order()
    # track -> RECEIVED
    track_order(order_ctx, "RECEIVED")
    # ord_rec latest assert
    assert_latest_topic("rec", order_ctx["orderId"])
    # cancel user
    cancel_order(order_ctx)
    # ord_can latest assert
    assert_latest_topic("can", order_ctx["orderId"])
    # track order -> status and expire
    track_order(order_ctx, "USER_CANCELLED")
    # send payment success
    send_message("pay", order_ctx)
    # track -> USER_CAN
    track_order(order_ctx, "USER_CANCELLED")
    # send ord cnf
    send_message("cnf", order_ctx)
    # track -> USER_CAN
    track_order(order_ctx, "USER_CANCELLED")
    print("completed scenario 4")

    # create order
    order_ctx = create_order()
    # track -> RECEIVED
    track_order(order_ctx, "RECEIVED")
    # ord_rec latest assert
    assert_latest_topic("rec", order_ctx["orderId"])
    # cancel restaurant via ord_can
    send_message("can", order_ctx)
    # track order -> status and expire
    track_order(order_ctx, "RESTAURANT_CANCELLED")
    # send payment success
    send_message("pay", order_ctx)
    # TODO: notify refund
    # track -> REST_CAN
    track_order(order_ctx, "RESTAURANT_CANCELLED")
    print("completed scenario 5")

    # invalid order create(invalid model)
    create_order_invalid([""])
    # invalid order cancel(repeated and invalid id)
    cancel_order(order_ctx, errors=["cancelled"])
    order_ctx["orderId"] = "abcd"
    cancel_order(order_ctx, errors=["not a valid"])
    # invalid order track(invalid id)
    order_ctx["orderId"] = "abcd"*6
    track_order(order_ctx, "", errors=["Invalid order id."])
    
    print("completed negative scenarios")
