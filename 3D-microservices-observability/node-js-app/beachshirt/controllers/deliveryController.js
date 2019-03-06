/**
* Driver for delivery service which is responsible to deliver the shirts of a given style.
* @author Yogesh Prasad Kurmi (ykurmi@vmware.com)
*/
const uuid4 = require('uuid4');

module.exports = (app, log) => {

    app.post('/dispatch/:orderNum/', (req, res) => {
        let orderNum = req.params.orderNum;
        let shirts = JSON.parse(req.body.shirts);
        if (Math.floor(Math.random() * 5) === 0) {
            const error = "Failed to dispatch shirts!";
            log.error(error);
            return res.status(503).json({ error: error });
        }

        if (Math.floor(Math.random() * 10) === 0) {
            const error = "Invalid Order Num";
            log.error(error);
            return res.status(400).json({ error: error });
        }

        if (Math.floor(Math.random() * 10) === 0) {
            shirts = null;
        }
        if (shirts === null || shirts.length === 0){
            const error = "No shirts to deliver";
            log.info(error);
            return res.status(400).json({ error: error });
        }
        trackingNum = uuid4()

        log.debug(`Tracking number of Order:${orderNum} is ${trackingNum}`);
        return res.json({orderNum, trackingNum, message:"shirts delivery dispatched"})
    });

    app.post('/retrieve/:orderNum/', (req, res) => {
        let orderNum = req.params.orderNum;
        if (orderNum === null){
            const error = "Invalid Order Num";
            log.error(msg);
            return res.status(400).json({ error: error });
        }
        return res.json({message:`Order: ${orderNum} returned`})
    });
};