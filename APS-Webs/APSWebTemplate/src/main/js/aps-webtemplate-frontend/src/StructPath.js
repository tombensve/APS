//
// PORTED FROM GROOVY (APSCoreLib)
//
/**
 * This handles a structures map key that can be navigated down and upp.
 *
 * The key format is: key.sub-key.sub-key. Each key that has a sub-key returns a Map and the sub-key
 * references a key in the returned map.
 */
export default class StructPath {

    //
    // Constructors
    //

    /**
     * Creates a new StructuredMapKey instance.
     *
     * @param startPath The starting path.
     */
    constructor( startPath: string ) {

        this.path = startPath;
    }

    /**
     * Copy constructor.
     *
     * @param structPath The MapPath to copy.
     */
    static copy( structPath ) {
        return new StructPath(structPath);
    }

    //
    // Methods
    //

    /**
     * Moves down the Map structure by providing a subkey that should return a Map.
     *
     * @param subPath The key of the sub map to enter.
     *
     * @return A new StructuredKey representing the new path.
     */
    down( subPath: string ) {

        return new StructPath( this.path.empty ? subPath : `${this.path}.${subPath}` );
    }

    /**
     * @return a new StructuredKey that represents the parent node.
     */
    up() {
        let lastDot: number = this.path.lastIndexOf( '.' );

        if ( lastDot === -1 ) {
            throw new Error( "Already at root! Can't go further up." );
        }

        return new StructPath( this.path.substring( 0, lastDot ) );
    }

    /**
     * @return The sub path key at the far right.
     */
    get right(): string {

        let lastDot: number = this.path.lastIndexOf( '.' );

        if ( lastDot === -1 ) {
            return this.path;
        }

        return this.path.substring( lastDot + 1 );
    }

    /**
     * @return true if the current right is an array.
     */
    isRightArray(): boolean {

        return this.right.startsWith("[");
    }

    /**
     * @return The size of the array if the right entry is an array. Otherwise -1 is returned.
     */
    rightArraySize(): number {
        let size: number = -1;

        if (this.isRightArray()) {

            size = parseInt(this.right.replace("[", "").replace("]", ""));
        }
        return size;
    }

    /**
     * @return true if at root, false otherwise.
     */
    isAtRoot(): boolean {
        return this.path.length === 1;
    }

    /**
     * @return A '.' separated full key as a String.
     */
    toString(): string {

        return this.path;
    }

    /**
     * @return the path as an array of its parts.
     */
    toParts(): [string] {

        return this.path.split("\\.");
    }

}
