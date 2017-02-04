/*
 * Defines local models.
 */

/**
 * Represents a separate admin app that will be rendered within a tab in the general admin app. Admin webs register
 * themselves on the server side on APSAdminWebService and the mail AdminWeb app fetches registered webs from that
 * service and renders them in separate tabs.
 */
export class AdminAppModel {

   constructor( public name: string , public url: string ) {}
}
