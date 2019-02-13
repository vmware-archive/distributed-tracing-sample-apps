/**
* Driver for delivery service which is responsible to deliver the shirts of a given style.
* @author Yogesh Prasad Kurmi (ykurmi@vmware.com)
*/
const uuidv1 = require('uuid/v1');

module.exports = (app, log) => {

    app.post('/dispatch/:orderNum/', (req, res) => {
        let orderNum = req.params.orderNum;
        let shirts = JSON.parse(req.body.shirts);
        if (Math.floor(Math.random() * 5) == 0) {
            let msg = "Failed to order shirts!";
            log.error(msg);
            return res.status(503).json({ error: msg });
        }

        if (Math.floor(Math.random() * 10) == 0) {
            let msg = "Invalid Order Num";
            log.error(msg);
            return res.status(400).json({ error: msg });
        }

        if (Math.floor(Math.random() * 10) == 0) {
            shirts = null;
        }
        if (shirts == null || shirts.length == 0){
            let msg = "No shirts to deliver";
            log.info(msg);
            return res.status(400).json({ error: msg });
        }
        trackingNum = uuidv1()

        log.debug(`Tracking number of Order:${orderNum} is ${trackingNum}`);
        return res.json({orderNum:orderNum, trackingNum:trackingNum, message:"shirts delivery dispatched"})
    });

    app.post('/retrieve/:orderNum/', (req, res) => {
        let orderNum = req.params.orderNum;
        if (orderNum == null){
            let msg = "Invalid Order Num";
            log.error(msg);
            return res.status(400).json({ error: msg });
        }
        return res.json({message:`Order:${orderNum}returned`})
    });
};