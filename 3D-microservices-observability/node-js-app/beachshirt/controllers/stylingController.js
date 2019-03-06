/**
* Driver for styling service which manages different styles of shirts and takes orders for a shirts
* of a given style.
* @author Yogesh Prasad Kurmi (ykurmi@vmware.com)
*/
const request = require('request');
const uuid4 = require('uuid4');
const utils = require('../utils');

module.exports = (app, config, log) => {
    app.get('/style', (req, res) => {
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

    app.get('/style/:id/make', (req, res) => {
        if (Math.floor(Math.random() * 10) === 0) {
            const error = "Failed to make shirts!";
            log.error(error);
            return res.status(503).json({ error: error });
        }
        let shirts = [];
        for(let i = 0; i < parseInt(req.query.quantity); i++){
            shirts.push({"name": req.params.id, "imageUrl":  `${req.params.id}-image`})
        }
        return utils.postRequest(res,
            `/dispatch/${uuid4()}`,
            { shirts: JSON.stringify(shirts) },
            config.delivery)
    });
};