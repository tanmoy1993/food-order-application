import requests
import json
import random

#pip install kafka-python
from kafka import KafkaProducer
from kafka.errors import KafkaError

producer = KafkaProducer(bootstrap_servers=['172.17.0.1:9092'])
producer = KafkaProducer(value_serializer=lambda m: json.dumps(m).encode('ascii'))
headers = {'Content-Type': 'application/json'}

def create_buyer():
    suffix = str(random.randint(10000, 90000))
    body = """{
        "firstname": "FB name suffix",
        "lastname": "LB name suffix",
        "phones": [
            {
                
                "phoneNo": "+49123456suffix"
            },
            {
                
                "phoneNo": "+49123456suffix"
            }
        ],
        "prefPhoneIndex": 0,
        "prefAddrIndex": 0,
        "addresses": [
            {
                
                "addressLineNo1": "addressLineNosuffix",
                "addressLineNo2": null,
                "zipCode": 53855
            }
        ]
    }""".replace("suffix", suffix, 5)

    res = requests.post('http://localhost:8092/buyer', headers=headers, data=body)
    print(res.status_code)
    body = json.loads(res.text)
    print(json.dumps(body, indent=4))
    return [body["buyerId"], body]

def create_buyer_invalid():
    body = """{
    "firstname": "Buyer F5",
    "lastname": ""
    }"""

    res = requests.post('http://localhost:8092/buyer', headers=headers, data=body)
    print(res.status_code)
    print(json.dumps(json.loads(res.text), indent=4))
    return

def get_all_buyers():
    res = requests.get('http://localhost:8092/buyer')
    print(res.status_code)
    print(json.dumps(json.loads(res.text), indent=4))
    return

def update_buyer(id, body):
    body["addresses"][0]["addressLineNo2"] = "check"
    res = requests.put('http://localhost:8092/buyer/' + id, headers=headers, data=json.dumps(body))
    print(res.status_code)
    print(json.dumps(json.loads(res.text), indent=4))
    return

def get_buyer(id):
    res = requests.get('http://localhost:8092/buyer/' + id)
    print(res.status_code)
    print(json.dumps(json.loads(res.text), indent=4))
    return

def send_pref_contact(id):
    future = producer.send('fo_ord_cnf', {"buyerId": id}, headers=[("__TypeId__","ord_cnf".encode('utf-8'))])

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
    get_all_buyers()
    [id, buyer] = create_buyer()
    get_buyer(id)
    send_pref_contact(id)
    update_buyer(id, buyer)

    #invalid
    get_buyer("88a4afb0-7b64-4b9b-a807-8cf37451bd25")
    create_buyer_invalid()
    