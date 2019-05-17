import '../APSEventBus'

export default class APSModelsManager {

    constructor() {

        let bus = APSEventBus.getBus("default");

        bus.subscribe()
    }
}