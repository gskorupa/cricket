/*
 * Copyright 2018 Grzegorz Skorupa <g.skorupa at gmail.com>.
 * Licensed under the Apache License http://www.apache.org/licenses/LICENSE-2.0
 */

route(function (id) {
    if (app.languages.indexOf(id) > -1) {
        app.language = id
        globalEvents.trigger('language')
        riot.mount('raw')
        riot.update()
        return
    }

    if (app.docPath) {
        app.previousPath = app.docPath
    }
    if (id.startsWith('articles')) {
        app.currentPage = 'articles'
        app.docPath = id.substring(8).replace(/,/g, "/");
    } else {
        app.currentPage = id
    }
    globalEvents.trigger('pageselected')
    riot.update()
})
