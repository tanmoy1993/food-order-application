let result = [
    db.getSiblingDB('DS_FO_RESTAURANT'),
    db.createCollection('FO_RESTAURANT_USER'),
    db.createCollection('FO_RESTAURANT_RESTAURANT'),
    db.createCollection('FO_RESTAURANT_NEARBY'),
    db.createCollection('FO_RESTAURANT_FOODITEM'),    
    db.FO_RESTAURANT_USER.createIndex({USER_ID: 1}, {unique: true}),
    db.FO_RESTAURANT_RESTAURANT.createIndex({USER_DB_ID: 1}),
    db.FO_RESTAURANT_NEARBY.createIndex({ZIP: 1}),
    db.FO_RESTAURANT_FOODITEM.createIndex({RES_ID: 1}),
    db.FO_RESTAURANT_USER.getIndexes()
]

printjson(result)

