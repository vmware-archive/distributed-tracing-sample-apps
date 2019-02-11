/**
* Driver for Shopping service provides consumer facing APIs supporting activities like browsing
* different styles of beachshirts, and ordering beachshirts.
* @author Yogesh Prasad Kurmi (ykurmi@vmware.com)
*/
const request = require('request');

module.exports = function(app, config, log){

    app.get('/shop/menu', function(req, res){
        request.get("http://" + config.styling.host + ":" + config.styling.port + "/style",
        function(error, httpResponse, body) {
            return res.json(JSON.parse(body));
        });
    });

    app.post('/shop/order', function(req, res){
        if (Math.floor(Math.random() * 10) == 0) {
            msg = "Failed to order shirts!";
            log.error(msg);
            return res.status(503).json({ error: msg });
        }

        request.get({
            url: "http://" + config.styling.host + ":" + config.styling.port + "/style/" + req.body.styleName + "/make",
            headers: {"Content-Type":"application/json"},
            qs: {"quantity":req.body.quantity}},
            function(error, response, body) {
                if (response.statusCode != 200){
                    return res.status(response.statusCode).json(JSON.parse(body));
                }
            return res.json(JSON.parse(body));
        });
    });
};  