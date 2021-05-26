from kafka import KafkaProducer
from kafka.errors import KafkaError
import json

producer = KafkaProducer(bootstrap_servers=['172.17.0.1:9092'])
producer = KafkaProducer(value_serializer=lambda m: json.dumps(m).encode('ascii'))
headers = {'Content-Type': 'application/json'}

def send_payment_status(id):
    future = producer.send('fo_pay_sta', \
        {"orderId": id, "status" : True, "message": ""}, \
            headers=[("__TypeId__","pay_sta".encode('utf-8'))])

    try:
        record_metadata = future.get(timeout=10)
    except KafkaError:
        log.exception()
        pass

    print (record_metadata.topic)
    print (record_metadata.partition)
    print (record_metadata.offset)
    return

def send_payment_status_failed(id):
    future = producer.send('fo_pay_sta', \
        {"orderId": id, "status" : False, "message": "user cancelled"}, \
            headers=[("__TypeId__","pay_sta".encode('utf-8'))])

    try:
        record_metadata = future.get(timeout=10)
    except KafkaError:
        log.exception()
        pass

    print (record_metadata.topic)
    print (record_metadata.partition)
    print (record_metadata.offset)
    return

if __name__=="__main__":
    send_payment_status("5edf7888485fe20001716099")
    send_payment_status_failed("5edf7888485fe20001716099")
