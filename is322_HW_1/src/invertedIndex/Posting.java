/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

/**
 *
 * @author ehab
 */
 
public class Posting {

    public Posting next;
    int docId;
    int dtf = 1;

    Posting(int id, int t) {
        docId = id;
        dtf=t;
        next = null;
    }
    
    Posting(int id) {
        docId = id;
        next = null;
    }

    public Posting add(int docId){
        if (this.docId == -1){
            this.docId = docId;
        }
        next = new Posting(-1);
        return next;

    }
}