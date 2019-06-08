package se.natusoft.osgi.aps.web.models

class APSTreeNode extends APSContainerComponent<APSTreeNode> {

    enum Type { branch, leaf }

    APSTreeNode setLabel(String label) {
        this.componentProperties.label = label
        this
    }

    APSTreeNode setType(Type type) {
        this.componentProperties.type = type.name(  )
        this
    }

    APSTreeNode setOpen(boolean open) {
        this.componentProperties.open = open
        this
    }
}
