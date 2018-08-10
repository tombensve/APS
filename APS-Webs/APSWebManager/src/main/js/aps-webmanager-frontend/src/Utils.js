/**
 * Checks the value if it contains any of the strings in contains array or is blank.
 *
 * @param {string} value The value to check.
 * @param {array.<string>} contains An array of valid strings for the value to contain. Any hit on these will return true.
 */
export function containsAnyOrBlank(value, contains) {
    let found = false;

    if (value === "") {
        found = true;
    }
    else {
        for (let part of contains) {
            if (value.indexOf(part) !== -1) {
                found = true;
                break;
            }
        }
    }

    return found;
}