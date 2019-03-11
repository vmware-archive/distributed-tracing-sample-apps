/**
* Driver for styling service which manages different styles of shirts and takes orders for a shirts
* of a given style.
* @author Yogesh Prasad Kurmi (ykurmi@vmware.com)
*/
const uuid4 = require('uuid4');
const utils = require('../utils');
const { Tags, FORMAT_HTTP_HEADERS } = require('opentracing');

module.exports = (app, config, log, tracer) => {
    app.get('/style', (req, res) => {
        const parentSpanContext = tracer.extract(FORMAT_HTTP_HEADERS, req.headers)
        const span = tracer.startSpan('/style', {
            childOf: parentSpanContext,
            tags: {[Tags.SPAN_KIND]: Tags.SPAN_KIND_RPC_SERVER}
        });
        setTimeout(() => {
            span.setTag(Tags.HTTP_STATUS_CODE, 200)
            span.finish();
            res.json(
                [
                    {
                        name: "Wavefront",
                        url: "WavefrontURL"
                    },
                    {
                        name: "BeachOps",
                        url: "BeachOpsURL"
                    }
                ]
            )}, 1000);
    });

    app.get('/style/:id/make', (req, res) => {
        const parentSpanContext = tracer.extract(FORMAT_HTTP_HEADERS, req.headers);
        const span = tracer.startSpan('/style/make', {
            childOf: parentSpanContext,
            tags: {[Tags.SPAN_KIND]: Tags.SPAN_KIND_RPC_SERVER}
        });
        if (Math.floor(Math.random() * 10) === 0) {
            const error = "Failed to make shirts!";
            log.error(error);
            span.setTag(Tags.HTTP_STATUS_CODE, 503);
            span.setTag(Tags.ERROR, true);
            span.finish();
            return res.status(503).json({error});
        }
        let shirts = [];
        quantity = parseInt(req.query.quantity)
        for(let i = 0; i < quantity; i++){
            shirts.push({name: req.params.id, imageUrl: `${req.params.id}-image`});
        }
        return utils.postRequest(res,
            `/dispatch/${uuid4()}`,
            { shirts: JSON.stringify(shirts) },
            config.delivery, span, tracer)
    });
};