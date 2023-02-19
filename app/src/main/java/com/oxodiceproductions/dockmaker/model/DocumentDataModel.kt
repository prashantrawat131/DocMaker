package com.oxodiceproductions.dockmaker.model

/*1)Size stores the size of the document
      * 2)Number of pics tells the total number of pics
      * available in the document
      * 3)Check is used for checkbox in the ui
      * which is used to select multiple document in the listView
      * so the user can delete all documents at the same time
      * by selecting them
      * Sample image id is used to store the thumbnail image for each document
      * */


class DocumentDataModel(
    var isCheck: Boolean,
    var docId: Long,
    var sampleImageId: String,
    var dateCreated: String,
    var timeCreated: String,
    var docName: String,
    var size: String,
    var numberOfPics: String
) :
    Comparable<DocumentDataModel?> {

    init {
        isCheck = isCheck
    }


    override fun compareTo(o: DocumentDataModel?): Int {

        //this comparator only compares date not time
        val d1Arr = dateCreated.split("-").toTypedArray()
        val d2Arr = o!!.dateCreated.split("-").toTypedArray()
        val yearCompare = d2Arr[2].toInt() - d1Arr[2].toInt()
        val monthCompare = d2Arr[1].toInt() - d1Arr[1].toInt()
        val dateCompare = d2Arr[0].toInt() - d1Arr[0].toInt()
        val finalDateCompare = compareDate(yearCompare, monthCompare, dateCompare)
        return if (finalDateCompare == 0) {
            //the date for both the docs is same now using time as a parameter
            val t1Arr = timeCreated.split(":").toTypedArray()
            val t2Arr = o.timeCreated.split(":").toTypedArray()
            val hourCompare = t2Arr[0].toInt() - t1Arr[0].toInt()
            val minuteCompare = t2Arr[1].toInt() - t1Arr[1].toInt()
            val secondCompare = t2Arr[2].toInt() - t1Arr[2].toInt()
            compareTime(hourCompare, minuteCompare, secondCompare)
        } else {
            finalDateCompare
        }
    }

    private fun compareTime(hourCompare: Int, minuteCompare: Int, secondCompare: Int): Int {
        return if (hourCompare == 0) {
            //year is same
            if (minuteCompare == 0) {
                //month is same
                secondCompare
            } else {
                //month not same
                minuteCompare
            }
        } else hourCompare
    }

    private fun compareDate(yearCompare: Int, monthCompare: Int, dateCompare: Int): Int {
        return if (yearCompare == 0) {
            //year is same
            if (monthCompare == 0) {
                //month is same
                dateCompare
            } else {
                //month not same
                monthCompare
            }
        } else yearCompare
    }
}