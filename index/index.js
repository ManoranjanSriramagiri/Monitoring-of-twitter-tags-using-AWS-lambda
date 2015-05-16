console.log('Loading function');

exports.handler = function(event, context) {
    var MongoClient = require('mongodb').MongoClient;
    MongoClient.connect("mongodb://test:test@ds031952.mongolab.com:31952/twitter", function(err, db) {
        if(err) {
            console.log("Unable to connect to mongodb server");
            context.fail ("Error connecting to mongodb: " + err)
        }
        else {
            console.log("Connection established to mongodb");

            var collection = db.collection('tweets');

            var myTweets = event.tweet;
            console.log(myTweets);

            for(var i=0;i<myTweets.length;i++) {
                var obj = myTweets[i];
                console.log(obj);
                collection.insert([obj], function (err, result) {
                    if (err) {
                        console.log(err);
                    }
                });
            }
            db.close();
            context.succeed('Insertion is successful');
        }
    });
};