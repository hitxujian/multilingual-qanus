package ar.uba.dc.galli.qa.ml.utils;

public enum EnumTypes
{
    NOVALUE,                         //Error
    
    LOCATION, ORGANIZATION, PERSON, OTHER, //NERC
    
    HUM, DESC, ENTY, LOC, NUM, ABBR,  //QC Stanford Clasess 
    WHO, WHOM, WHERE, WHICH, //asked entity
    WHEN,WHAT,
    ind,gr,title,desc,               //HUM subclasses
    state,other,city,country,mount,  //LOC subclasses
    exp,abb,                         //ABBR subclasses
    manner,reason,def, //desc,          //DESC subclasses    
    substance,sport,plant,techmeth,cremat,animal,event,letter,religion,food,product,color,termeq,body,dismed,instru,word,lang,symbol,veh,currency, //other,//ENTY subclasses
    date,count,money,period,volsize,speed,perc,code,dist,temp,ord,weight; //other,//NUM subclasses
    
    
    public static EnumTypes val(String str)
    {
        try {
            return valueOf(str);
        } 
        catch (IllegalArgumentException ex) {
            //System.out.println("Error en Switchy: "+str);
            return NOVALUE;
        }
    }    
}