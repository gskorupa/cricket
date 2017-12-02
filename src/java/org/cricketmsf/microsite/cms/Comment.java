/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cricketmsf.microsite.cms;

import java.util.Date;

/**
 *
 * @author greg
 */
public class Comment {
    String uid;
    String documentUid;
    String text;
    Date created;
    boolean accepted;
    String authorUid;
}
