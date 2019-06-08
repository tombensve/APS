package se.natusoft.osgi.aps.web.models

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This component displays markdown text.
 *
 * Note that in addition to setting value (with § forcing new paragraph in one line strings) this
 * component also listens to messages of aps.type "md-text", and content.targetId same as component id.
 * If such is received the content.markdown will be used as text for the component.
 *
 * Example of message:
 *     {
 *         aps: {
 *             type: "md-text"
 *         },
 *         content: {
 *             targetId: "id",
 *             markdown: "markdown text"
 *         }
 *     }
 */
@CompileStatic
@TypeChecked
class APSMarkdown extends APSComponent {

    APSMarkdown() {
        this.componentProperties.type = "aps-markdown"
    }
}
