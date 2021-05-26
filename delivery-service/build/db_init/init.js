let result = [
    db.getSiblingDB('DS_FO_DELIVERY'),
    db.createCollection('FO_DELIVERY_CTX'),   
    db.FO_DELIVERY_CTX.createIndex({RestaurantId: 1}),
    db.FO_DELIVERY_CTX.createIndex( { "ExpireOn": 1 }, { expireAfterSeconds: 0 } ),
    db.FO_DELIVERY_CTX.getIndexes(),
    db.FO_DELIVERY_CTX.insert( { "buyerId": "dummy", "ExpireOn": new Date("2030-12-30") } ),
    db.FO_DELIVERY_CTX.getIndexes(),
]

printjson(result)

