/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.util.ArrayList;

/**
 *
 * @author ehab
 */
 
public class Posting {

    public Posting next;
    public int docId;
    public int dtf = 1;
    ArrayList<Integer> positions;

    Posting(int id, int t) {
        docId = id;
        dtf=t;
        next = null;
        positions= new ArrayList<>();

    }

    Posting(int id) {
        docId = id;
        next = null;
        positions= new ArrayList<>();

    }
    public void addPosition(Integer position){
        positions.add(position);
    }

//    public Posting add(int docId){
//        if (this.docId == -1){
//            this.docId = docId;
//        }
//        next = new Posting(-1);
//        return next;
//
//    }
}