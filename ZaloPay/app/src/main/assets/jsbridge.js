var callbackPool = {};

function initializeWebBridge() {
  console.log('execute to inject script');

  // call
  AlipayJSBridge.call = function (func, param, callback) {
    if ('string' !== typeof func) {
      return;
    }

    if ('function' === typeof param) {
      callback = param;
      param = null;
    } else if (typeof param !== 'object') {
      param = null;
    }
    var clientId = '' + new Date().getTime()+(Math.random());
    if ('function' === typeof callback) {
      callbackPool[clientId] = callback;
    }
    var invokeMsg = JSON.stringify({
      func: func,
      param: param,
      msgType: 'call',
      clientId: clientId
    });
    console.log("AlipayJSBridge.callNativeFunction: " + invokeMsg);
    AlipayJSBridge.callNativeFunction("call", invokeMsg);
  }

  // callback
  AlipayJSBridge.callback = function (clientId, param) {
    var invokeMsg = JSON.stringify({
      clientId: clientId,
      param: param
    });
    console.log("AlipayJSBridge.callNativeFunction: " + invokeMsg);
    AlipayJSBridge.callNativeFunction("callback", invokeMsg);
  }

  // trigger
  AlipayJSBridge.trigger = function (name, param, clientId) {
    if (name) {
      var evt = document.createEvent('Events');
      evt.initEvent(name, false, true);
      if (typeof param === 'object') {
        for (var k in param) {
          evt[k] = param[k];
        }
      }
      evt.clientId = clientId;
      var prevent = !document.dispatchEvent(evt);
      if (clientId && name === 'back') {
        AlipayJSBridge.callback(clientId, {prevent: prevent});
      }if (clientId && name === 'firePullToRefresh') {
        AlipayJSBridge.callback(clientId, {prevent: prevent});
      }
    }
  }

  // _invokeJS
  AlipayJSBridge._invokeJS = function (resp) {
    resp = JSON.parse(resp);
    console.log("invokeJS msgType " + resp.msgType + " func " + resp.func);
    if (resp.msgType === 'callback') {
      var func = callbackPool[resp.clientId];
      if (!(typeof resp.keepCallback == 'boolean' && resp.keepCallback)) {
        delete callbackPool[resp.clientId];
      }
      if ('function' === typeof func) {
        setTimeout(function () {
          func(resp.param);
        }, 1);
      }
    } else if (resp.msgType === 'call') {
      resp.func && this.trigger(resp.func, resp.param, resp.clientId);
    }
  }

  var readyEvent = document.createEvent('Events');
  readyEvent.initEvent('AlipayJSBridgeReady', false, false);
  document.dispatchEvent(readyEvent);
};


