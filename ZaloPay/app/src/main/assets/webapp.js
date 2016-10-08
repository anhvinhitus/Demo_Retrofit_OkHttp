'use strict';

function webapp_getNavigation() {
    if (typeof(utils) === "undefined") {
        return {
            returnCode: 0
        };
    }

    if (typeof(utils.getNav) === "undefined") {
        return {
            returnCode: 0
        }
    }

    if (typeof(utils.back) === "undefined") {
        return {
          nav: utils.getNav(),
          returnCode: 2
        };
    }

    return {
      nav: utils.getNav(),
      returnCode: 1
    };
}

