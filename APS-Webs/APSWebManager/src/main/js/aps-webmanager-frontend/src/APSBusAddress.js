import uuid from './APSUUID'

export default class APSBusAddress {

    /**
     * This creates an instance of APSAddress that has the following properties containing address to use.
     *
     * - client
     * - backend
     * - clusterAll
     * - clusterBackends
     * - clusterClients
     *
     * @param {string} app - The name of the app the addresses are for.
     */
    constructor(app) {
        this.client = "aps:" + app + ":client:" + uuid();
        this.backend = "aps:" + app + ":backend";
        this.all = "aps:" + app + ":all";
        this.allBackends = "aps:" + app + ":all:backend";
        this.allClients = "aps:" + app + ":all:client";
    }
}