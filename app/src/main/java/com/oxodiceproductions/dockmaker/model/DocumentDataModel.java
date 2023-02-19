package com.oxodiceproductions.dockmaker.model;

public class DocumentDataModel implements Comparable<DocumentDataModel> {
    long DocId;
    String SampleImageId;
    String DateCreated;
    String TimeCreated;
    String DocName;
    String Size;
    String NumberOfPics;
    boolean check;

    /*1)Size stores the size of the document
     * 2)Number of pics tells the total number of pics
     * available in the document
     * 3)Check is used for checkbox in the ui
     * which is used to select multiple document in the listView
     * so the user can delete all documents at the same time
     * by selecting them
     * Sample image id is used to store the thumbnail image for each document
     * */

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public DocumentDataModel(boolean check, long docId, String sampleImageId, String dateCreated, String timeCreated, String docName, String size, String NumberOfPics) {
        DocId = docId;
        SampleImageId = sampleImageId;
        DateCreated = dateCreated;
        TimeCreated = timeCreated;
        DocName = docName;
        Size = size;
        this.NumberOfPics = NumberOfPics;
        this.check = check;
    }

    public long getDocId() {
        return DocId;
    }


    public String getDateCreated() {
        return DateCreated;
    }


    public String getTimeCreated() {
        return TimeCreated;
    }




    public String getDocName() {
        return DocName;
    }


    public String getSize() {
        return Size;
    }


    public String getNumberOfPics() {
        return NumberOfPics;
    }

    public String getSampleImageId() {
        return SampleImageId;
    }

    public void setSampleImageId(String sampleImageId) {
        SampleImageId = sampleImageId;
    }

    @Override
    public int compareTo(DocumentDataModel o) {

        //this comparator only compares date not time
        String[] d1Arr = this.getDateCreated().split("-");
        String[] d2Arr = o.getDateCreated().split("-");

        int yearCompare = Integer.parseInt(d2Arr[2]) - (Integer.parseInt(d1Arr[2]));
        int monthCompare = Integer.parseInt(d2Arr[1]) - (Integer.parseInt(d1Arr[1]));
        int dateCompare = Integer.parseInt(d2Arr[0]) - (Integer.parseInt(d1Arr[0]));

        int finalDateCompare = compareDate(yearCompare, monthCompare, dateCompare);

        if (finalDateCompare == 0) {
            //the date for both the docs is same now using time as a parameter
            String[] t1Arr = this.getTimeCreated().split(":");
            String[] t2Arr = o.getTimeCreated().split(":");

            int hourCompare = Integer.parseInt(t2Arr[0]) - (Integer.parseInt(t1Arr[0]));
            int minuteCompare = Integer.parseInt(t2Arr[1]) - (Integer.parseInt(t1Arr[1]));
            int secondCompare = Integer.parseInt(t2Arr[2]) - (Integer.parseInt(t1Arr[2]));

            return compareTime(hourCompare, minuteCompare, secondCompare);
        } else {
            return finalDateCompare;
        }
    }

    private int compareTime(int hourCompare, int minuteCompare, int secondCompare) {
        if (hourCompare == 0) {
            //year is same
            if (minuteCompare == 0) {
                //month is same
                return secondCompare;
            } else {
                //month not same
                return minuteCompare;
            }
        }

        return hourCompare;
    }

    private int compareDate(int yearCompare, int monthCompare, int dateCompare) {
        if (yearCompare == 0) {
            //year is same
            if (monthCompare == 0) {
                //month is same
                return dateCompare;
            } else {
                //month not same
                return monthCompare;
            }
        }

        return yearCompare;
    }
}
