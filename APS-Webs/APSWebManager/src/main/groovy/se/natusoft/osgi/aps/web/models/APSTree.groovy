package se.natusoft.osgi.aps.web.models

class APSTree extends APSComponent<APSTree> {

    APSTree() {
        this.componentProperties.type = "aps-tree"
    }

    APSTree node(APSTreeNode node) {
        this.componentProperties.node = node.componentProperties
        this
    }

    APSTree setNodes(APSTreeNode nodes) {
        node(nodes)
    }
}
