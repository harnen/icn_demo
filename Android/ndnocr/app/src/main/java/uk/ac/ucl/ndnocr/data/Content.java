package uk.ac.ucl.ndnocr.data;

/**
 * Created by srenevic on 24/08/17.
 *
 * A class representing a video content object with its attributes
 *
 */

public class Content {

    //private variables
    String _name;
    String _url;
    String _text;
    String _uri;

    // Empty constructor
    public Content(){

    }
    // constructor
    public Content(String name, String uri, String text, String url){
      //  this._id = id;
        this._name = name;
        this._uri = uri;
        this._text = text;
        this._url = url;
    }

    // getting name
    public String getName(){
        return this._name;
    }

    // setting name
    public void setName(String name){
        this._name = name;
    }

    // getting description
    public String getText(){
        return this._text;
    }

    // setting description
    public void setText(String text){
        this._text = text;
    }

    // getting url
    public String getUrl(){
        return this._url;
    }

    // setting url
    public void setUrl(String url){
        this._url = url;
    }


    // getting name
    public String getUri(){
        return this._uri;
    }


    // setting name
    public void setUri(String uri){
        this._uri = uri;
    }
}
