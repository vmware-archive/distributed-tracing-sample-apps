/**
* Driver for delivery service which is responsible to deliver the shirts of a given style.
* @author Yogesh Prasad Kurmi (ykurmi@vmware.com)
*/
const uuid4 = require('uuid4');
const utils = require('../utils');
const { Tags, FORMAT_HTTP_HEADERS } = require('opentracing');

module.exports = (app, log, tracer) => {

    app.post('/dispatch/:orderNum/', (req, res) => {
        const parentSpanContext = tracer.extract(FORMAT_HTTP_HEADERS, req.headers)
        const span = tracer.startSpan('/dispatch', {
            childOf: parentSpanContext,
            tags: {...{[Tags.SPAN_KIND]: Tags.SPAN_KIND_RPC_SERVER}, ...utils.getCustomTags()}
        });
        const orderNum = req.params.orderNum;
        let shirts = JSON.parse(req.body.shirts);
        if (utils.getRandomInt(5) === 0) {
            const error = "Failed to dispatch shirts!";
            log.error(error);
            span.setTag(Tags.HTTP_STATUS_CODE, 503)
            span.setTag(Tags.ERROR, true)
            span.finish();
            return res.status(503).json({error});
        }

        if (utils.getRandomInt(10) === 0) {
            const error = "Invalid Order Num";
            log.error(error);
            span.setTag(Tags.HTTP_STATUS_CODE, 400)
            span.setTag(Tags.ERROR, true)
            span.finish();
            return res.status(400).json({error});
        }

        if (utils.getRandomInt(10) === 0) {
            shirts = null;
        }
        if (shirts === null || shirts.length === 0){
            const error = "No shirts to deliver";
            log.info(error);
            span.setTag(Tags.HTTP_STATUS_CODE, 400)
            span.setTag(Tags.ERROR, true)
            span.finish();
            return res.status(400).json({error});
        }
        trackingNum = uuid4()

        log.debug(`Tracking number of Order:${orderNum} is ${trackingNum}`);
        setTimeout(() => {
            span.setTag(Tags.HTTP_STATUS_CODE, 200)
            span.finish();
            return res.json({orderNum, trackingNum, message:"shirts delivery dispatched"})
        }, 1000);
    });

    app.post('/retrieve/:orderNum/', (req, res) => {
        const span = tracer.startSpan('/retrieve', { tags: utils.getCustomTags() });
        const orderNum = req.params.orderNum;
        if (orderNum === null){
            const error = "Invalid Order Num";
            log.error(error);
            span.setTag(Tags.HTTP_STATUS_CODE, 400)
            span.setTag(Tags.ERROR, true)
            span.finish();
            return res.status(400).json({error});
        }
        span.setTag(Tags.HTTP_STATUS_CODE, 200)
        span.finish();
        return res.json({message:`Order: ${orderNum} returned`})
    });
};