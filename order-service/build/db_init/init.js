let result = [
    db.getSiblingDB('DS_FO_ORDER'),
    db.createCollection('FO_ORDER_CTX'),   
    db.FO_ORDER_CTX.createIndex( { "BuyerId" : 1 }),
    db.FO_ORDER_CTX.createIndex( { "ExpireOn": 1 }, { expireAfterSeconds: 0 } ),
    db.FO_ORDER_CTX.getIndexes(),
    db.FO_ORDER_CTX.insert( { "buyerId": "dummy", "ExpireOn": new Date("2030-12-30") } ),
    db.FO_ORDER_CTX.getIndexes(),
]

printjson(result)

