import requests
import json
import random
import time

from kafka import KafkaProducer
from kafka.errors import KafkaError

headers = {'Content-Type': 'application/json'}
baseUrl = "http://0.0.0.0:8101/"
sleep_time = 3

producer = KafkaProducer(bootstrap_servers=['172.17.0.1:9092'])
producer = KafkaProducer(value_serializer=lambda m: json.dumps(m).encode('ascii'))

def get_rand_long():
    return str(random.randint(10000, 99999))

def random_user(username):
    if username == None:
        username = "uname" + get_rand_long()
    return{"userName" : username, "email" : "email" + get_rand_long() + "@xx.com" , 
 				"phone" : "16550" + get_rand_long(), "passwordHash" : "passwordHash" + get_rand_long()}

def random_resturant(userId, zip, name=None):
    if name==None:
        name = "restname" + get_rand_long()
    return { "userId" : userId, 
             "restaurantName" : name, 
             "priceRangeMin" : 300, 
             "priceRangeMax" : 800,
             "zipCodesDelivered" : zip
             }
 	
def randomFI(restId) :
    return {"restaurantId" : restId, 
            "price" : get_rand_long(), 
            "name" : "foodName" + get_rand_long()}

def create_user(username=None, error=False):
    print("creating user")
    body = random_user(username)
    res = requests.post(baseUrl + "user", headers=headers, json=body)
    print(res.status_code)
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    if(error):
        assert res.status_code == 400
        assert "User name already exists." == response_body["message"]
        return
    #assert id and name
    assert res.status_code == 200
    assert body["userName"] == response_body["userName"]
    assert "id" in response_body
    assert response_body["passwordHash"] == "****"
    response_body["passwordHash"] = body["passwordHash"]
    time.sleep(sleep_time)
    return response_body

def create_user_invalid():
    print("creating invalid user")
    body = {"email" : "email" + get_rand_long() + "@xx.com" , 
 				"phone" : "", "passwordHash" : ""}

    res = requests.post(baseUrl + "user", headers=headers, json=body)
    print(res.status_code)
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))

    assert res.status_code == 400
    assert "User name can not be null or empty." + \
        "Password can not be null or empty." == response_body["message"]
    return

def login(username, password, error=False):
    print("login user")
    body = {"userName" : username, "passwordHash" : password}
    res = requests.post(baseUrl + "login", headers=headers, json=body)
    print(res.status_code)
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    if(error):
        assert res.status_code == 400
        assert "User name and password does not exist."  == response_body["message"]
        return
    #assert id and name
    assert res.status_code == 200
    assert body["userName"] == response_body["userName"]
    assert "id" in response_body
    assert response_body["passwordHash"] == "****"
    return response_body

def create_restaurant(user, zip):
    print("creating restaurant")
    body = random_resturant(user["id"], zip)
    res = requests.post(baseUrl + "restaurant", headers=headers, json=body)
    print(res.status_code)
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    assert res.status_code == 200
    #assert id and name
    assert body["restaurantName"] == response_body["restaurantName"]
    assert user["email"] == response_body["email"]
    assert "id" in response_body
    time.sleep(sleep_time)
    return response_body

def create_restaurant_invalid_user():
    print("creating restaurant invalid user")
    body = random_resturant("invalid_user_id", [1,2])
    res = requests.post(baseUrl + "restaurant", headers=headers, json=body)
    print(res.status_code)
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    assert res.status_code == 400
    #assert id and name
    assert "Invalid user Id."  == response_body["message"]
    return response_body

def create_restaurant_invalid_model(user):
    print("creating restaurant invalid model")
    body = random_resturant(user["id"], [], "")
    res = requests.post(baseUrl + "restaurant", headers=headers, json=body)
    print(res.status_code)
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    assert res.status_code == 400
    #assert id and name
    assert "Restaurant name can not be null or empty.At least one zip code needed."  \
                    == response_body["message"]
    return response_body

def update_restaurant(restaurant):
    print("updating restaurant: " + str(restaurant))
    res = requests.put(baseUrl + "restaurant", headers=headers, json=restaurant)
    print(res.status_code)
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    assert res.status_code == 200
    #assert id and name
    assert restaurant["restaurantName"] == response_body["restaurantName"]
    assert restaurant["id"] == response_body["id"]
    time.sleep(sleep_time)
    return response_body

def delete_restaurant(restaurant):
    print("delete restaurant")
    res = requests.delete(baseUrl + "restaurant", headers=headers, json=restaurant)
    print(res.status_code)
    print(res.text)
    assert res.status_code == 200
    time.sleep(sleep_time)
    return 

def get_restaurant_zip(zip, restaurants, error=False):
    print("get restaurant zip: " + str(zip))
    res = requests.get(baseUrl + "zip/" + str(zip) + "/restaurant", headers=headers)
    print(res.status_code)
    #print(res.text)
    if(error):
        for r in restaurants:
            assert r["id"] not in res.text
            assert r["restaurantName"] not in res.text
        return
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    assert res.status_code == 200
    #assert id and name
    for r in restaurants:
        assert r["id"] in res.text
        assert r["restaurantName"] in res.text
    return

def get_restaurant_user(uid, restaurants, error=False):
    print("get restaurant user")
    res = requests.get(baseUrl + "user/" + uid + "/restaurant", headers=headers)
    print(res.status_code)
    if(error):
        assert res.status_code == 400
        return
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    assert res.status_code == 200
    #assert id and name
    for r in restaurants:
        assert r["id"] in res.text
        assert r["restaurantName"] in res.text
    #assert len(restaurants) == len(response_body)
    return 

def create_food(restaurantId):
    print("creating food")
    body = [randomFI(restaurantId),randomFI(restaurantId),randomFI(restaurantId)]
    res = requests.post(baseUrl + "food", headers=headers, json=body)
    print(res.status_code)
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    assert res.status_code == 200
    #assert id and restid
    for i in range(3):
        assert restaurantId == response_body[i]["restaurantId"]
        assert "id" in response_body[i]
        #foodIds.append(response_body[i]["id"])
    time.sleep(sleep_time)
    return response_body

def update_food(food):
    print("update food: " + str(food))
    res = requests.put(baseUrl + "food", headers=headers, json=food)
    print(res.status_code)
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    assert res.status_code == 200
    #assert id and name
    assert food["name"] == response_body["name"]
    assert food["id"] == response_body["id"]
    time.sleep(sleep_time)
    return response_body

def delete_food(food):
    print("delete food")
    res = requests.delete(baseUrl + "food", headers=headers, json=food)
    print(res.status_code)
    print(res.text)
    assert res.status_code == 200
    time.sleep(sleep_time)
    return 

def get_food_restaurant(restaurantId, foods, error=False):
    print("get restaurant food")
    res = requests.get(baseUrl + "restaurant/" + restaurantId + "/food", headers=headers)
    print(res.status_code)
    response_body = json.loads(res.text)
    print(json.dumps(response_body, indent=4))
    if(error):
        assert res.status_code == 400
        return
    assert res.status_code == 200
    #assert id and name
    for f in foods:
        assert f["id"] in res.text
        assert f["name"] in res.text
    #assert len(response_body) == len(foods)
    return 

def send_ord_rec(restId, totalCost, foods):
    orderId = "3123adsad" + get_rand_long()
    future = producer.send('fo_ord_rec', \
        {"totalCost": totalCost, "orderId": orderId, "restaurantId" : restId,
         "orderedItems" : foods}, 
                    headers=[("__TypeId__","ord_rec".encode('utf-8'))])

    try:
        record_metadata = future.get(timeout=10)
    except KafkaError:
        log.exception()
        pass

    print (record_metadata.topic)
    print (record_metadata.partition)
    print (record_metadata.offset)
    return orderId

def send_ord_can(restId, reason):
    orderId = "3123adsad" + get_rand_long()
    future = producer.send('fo_ord_can', \
        {"orderId": orderId, "restaurantId" : restId,
         "reason" : reason}, headers=[("__TypeId__","ord_can".encode('utf-8'))])

    try:
        record_metadata = future.get(timeout=10)
    except KafkaError:
        log.exception()
        pass

    print (record_metadata.topic)
    print (record_metadata.partition)
    print (record_metadata.offset)
    return orderId

if __name__=="__main__":

    #create user
    user = create_user()

    #login bad password
    login(user["userName"], "pass", True)

    #repeated user name
    create_user(user["userName"], True)

    #login user
    login(user["userName"], user["passwordHash"])

    #invalid user model
    create_user_invalid()

    #create restaurant 1 - zip 1,2
    res1 = create_restaurant(user, [2001, 2002])

    #create restaurant 2 - zip 2,3
    res2 = create_restaurant(user, [2002, 2003])

    #create restaurant invalid user id
    create_restaurant_invalid_user()

    #create restaurant invalid model
    create_restaurant_invalid_model(user)

    #fetch by user
    get_restaurant_user(user["id"], [res1, res2])

    #fetch by zip - 1
    get_restaurant_zip(2001, [res1])

    #fetch by zip - 2
    get_restaurant_zip(2002, [res1, res2])

    #update rest 1 - zip 2,4 - name change
    res1["restaurantName"] = "updated name" + get_rand_long()
    res1["zipCodesDelivered"] = [2002, 2004]
    update_restaurant(res1)

    #fetch zip 1,2,4 - assert name
    get_restaurant_zip(2001, [res1], True)
    get_restaurant_zip(2002, [res1, res2])
    get_restaurant_zip(2004, [res1])

    #fetch rest by user
    get_restaurant_user(user["id"], [res1, res2])
    
    #delete rest 1
    delete_restaurant(res1)

    #fetch rest by user
    get_restaurant_user(user["id"], [res2])

    #fetch by zip 2,4
    get_restaurant_zip(2002, [res2])
    get_restaurant_zip(2004, [], True)

    #insert fi 1,2,3 for rest 2
    foods = create_food(res2["id"])

    #fetch by rest 2
    get_food_restaurant(res2["id"], foods)

    #update fi 2 name
    foods[1]["name"] = "updated fname" + get_rand_long()
    update_food(foods[1])

    #fetch by rest 2 - assert name change
    get_food_restaurant(res2["id"], foods)

    #order received
    foods[1]["qty"] = 2
    foods[2]["qty"] = 3
    total = foods[1]["qty"] * foods[1]["price"] + foods[2]["qty"] * foods[2]["price"]
    send_ord_rec(res2["id"], total, foods[1:])

    #order cancelled
    send_ord_can(res2["id"], "USER_CANCELLED")
    send_ord_can(res2["id"], "RESTAURANT_CANCELLED")
    time.sleep(sleep_time)

    #delete fi 1
    delete_food(foods[0])
    del foods[0]

    #fetch by rest 2 - assert
    get_food_restaurant(res2["id"], foods)

    #delete rest 2
    delete_restaurant(res2)

    #fetch by rest 2
    get_food_restaurant(res2["id"], [], True)

    #fetch rest by user
    get_restaurant_user(user["id"], [], True)

    print("finished")
