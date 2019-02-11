/**
* Driver for styling service which manages different styles of shirts and takes orders for a shirts
* of a given style.
* @author Yogesh Prasad Kurmi (ykurmi@vmware.com)
*/
const request = require('request');
const uuidv1 = require('uuid/v1');

module.exports = function(app, config, log){
    app.get('/style', function(req, res){
        res.json(
            [
                {
                    "name": "Wavefront",
                    "url": "WavefrontURL"
                },
                {
                    "name": "BeachOps",
                    "url": "BeachOpsURL"
                }
            ]
        );
    });

    app.get('/style/:id/make', function(req, res){
        if (Math.floor(Math.random() * 10) == 0) {
            msg = "Failed to order shirts!";
            log.error(msg);
            return res.status(503).json({ error: msg });
        }
        var shirts = [];
        var i;
        for(i = 0; i < parseInt(req.query.quantity); i++){
            shirts.push({"name": req.params.id, "imageUrl":  req.params.id + "-image"})
        }
        request.post({
            url: "http://" + config.delivery.host + ":" + config.delivery.port + "/dispatch/" + uuidv1(),
            headers: {"Content-Type":"application/json"},
            form:{ shirts: JSON.stringify(shirts) }
        },function(error, response, body) {
            if (response.statusCode != 200){
                return res.status(response.statusCode).json(JSON.parse(body));
            }
           return res.json(JSON.parse(body))
         })
    });
};