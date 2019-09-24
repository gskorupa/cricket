/*
 * Copyright 2018 Grzegorz Skorupa <g.skorupa at gmail.com>.
 * Licensed under the Apache License http://www.apache.org/licenses/LICENSE-2.0
 */

route(function (id) {
    console.log('ID:'+id)
    if (app.languages.indexOf(id) > -1) {
        app.language = id
        app.getTranslation()
        globalEvents.trigger('pageselected')
        riot.update()
        return
    }
    if(app.currentId){
        app.previousId = app.currentId        
    }else{
        app.previousId = ''
    }
    app.currentId = id

    var idx=id.indexOf(',')
    if(idx>-1){
        app.docPath = id.substring(idx+1).replace(/,/g, "/")
        app.currentPage = id.substring(0,idx)
    }else{
        app.docPath = id
        app.currentPage = id
    }
    
    globalEvents.trigger('pageselected')
    riot.update()
})
