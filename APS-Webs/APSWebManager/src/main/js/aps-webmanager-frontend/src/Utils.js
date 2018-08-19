/**
 * Checks if mobile device. This is of course not fail safe!! There seem to be no standard for mobile devices to
 * say that they are a mobile device.
 *
 * Use: isMobileDevice().any() or isMobileDevice().iOS(), ...
 *
 * Taken from: https://www.abeautifulsite.net/detecting-mobile-devices-with-javascript, and modified slightly.
 *
 * @returns {{Android: (function(): RegExpMatchArray), BlackBerry: (function(): RegExpMatchArray), iOS: (function(): RegExpMatchArray), Opera: (function(): RegExpMatchArray), Windows: (function(): RegExpMatchArray), any: (function(): *)}}
 */
export function isMobileDevice() {
    return {
        Android: function() {
            return navigator.userAgent.match(/Android/i);
        },
        BlackBerry: function() {
            return navigator.userAgent.match(/BlackBerry/i);
        },
        iOS: function() {
            return navigator.userAgent.match(/iPhone|iPad|iPod/i);
        },
        Opera: function() {
            return navigator.userAgent.match(/Opera Mini/i);
        },
        Windows: function() {
            return navigator.userAgent.match(/IEMobile/i);
        },
        any: function() {
            return (this.Android() || this.BlackBerry() || this.iOS() || this.Opera() || this.Windows());
        }
    };
}

// /**
//  * Returns true if the screen width is less than 1000.
//  *
//  * @returns {boolean}
//  */
// export function isSmallWidthDevice() {
//     return screen.width < 1000;
// }

function _sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * Sleeps for a specified period of time.
 * @param time
 * @returns {Promise<void>}
 */
export async function sleep(time) {
    console.log('Taking a break...');
    await _sleep(time);
    console.log('Break over');
}


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