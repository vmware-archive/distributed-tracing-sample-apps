/**
* Driver for delivery service which is responsible to deliver the shirts of a given style.
* @author Yogesh Prasad Kurmi (ykurmi@vmware.com)
*/
const uuidv1 = require('uuid/v1');

module.exports = function(app, log){

    app.post('/dispatch/:orderNum/', function(req, res){
        orderNum = req.params.orderNum;
        shirts = req.body.shirts;
        if (Math.floor(Math.random() * 5) == 0) {
            msg = "Failed to order shirts!";
            log.error(msg);
            return res.status(503).json({ error: msg });
        }

        if (Math.floor(Math.random() * 10) == 0) {
            orderNum = null;
        }

        if (orderNum == null){
            msg = "Invalid Order Num";
            log.error(msg);
            return res.status(400).json({ error: msg });
        }

        if (Math.floor(Math.random() * 10) == 0) {
            shirts = null;
        }

        if (shirts == null){
            msg = "No shirts to deliver";
            log.info(msg);
            return res.status(400).json({ error: msg });
        }
        trackingNum = uuidv1()

        log.debug("Tracking number of Order:" + orderNum + " is " + trackingNum);
        return res.json({orderNum:orderNum, trackingNum:trackingNum, message:"shirts delivery dispatched"})
    });

    app.post('/retrieve/:orderNum/', function(req, res){
        orderNum = req.params.orderNum;
        if (orderNum == null){
            msg = "Invalid Order Num";
            log.error(msg);
            return res.status(400).json({ error: msg });
        }
        return res.json({message:"Order:" + orderNum + " returned"})
    });
};