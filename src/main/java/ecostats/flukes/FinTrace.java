/**
 * @author Ecological Software Solutions LLC
 * @version 0.1 Alpha
 * @copyright 2014 
 * @license This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package com.ecostats.flukes;

import java.lang.reflect.Array;

/**
 * FinTrace
 * <p/>
 * Class for points along a fin tracing. Each point can be assigned 
 * an type that defines the type of feature on the fin the point represents. 
 * All points between a start and end point of any type should also be assigned
 * that type to define the extent of that type on the fin. This also allows
 * for extracting an exclusive subset of any type from a TreeHash containing
 *  many TracePoints.  
 */
public class FinTrace implements java.io.Serializable {
    
  private static final long serialVersionUID = -2795322614586425845L;
  
  /**
   * FinTrace Variables
   * <p/>
   * Comments: The constants initiated here and referenced
   * in the entire package as a FinTrace immutable value
   * for each point type.
   */
  public final static int DBL_INIT = -1000000000; // default XY point location if one is not provided at construction
  /* Mark Types */
  public final static int POINT = -2;         // normal point, no special notation
  public final static int TIP = -1;           // fin tip
  public final static int NOTCH = 0;          // fluke notch
  public final static int NICK = 1;           // small nick
  public final static int GOUGE = 2;          // large nick, start=2 end=3
  public final static int GOUGE_END= 3;       // large nick, start=2 end=3
  public final static int SCALLOP = 4;        // scallop, start=4 end=5
  public final static int SCALLOP_END = 5;    // scallop, start=4 end=5
  public final static int WAVE = 6;           // wave points, start=6 end=7
  public final static int WAVE_END = 7;       // wave points, start=6 end=7
  public final static int MISSING = 8;        // missing section, start=8 end=9
  public final static int MISSING_END = 9;    // missing section, start=8 end=9
  public final static int SCAR = 10;          // tooth scars on fin
  public final static int HOLE = 11;          // hole in fin
  public final static int INVISIBLE = 12;     // invisible parts, start=12 end=13  
  public final static int INVISIBLE_END = 13; // invisible parts, start=12 end=13
  private double[] x;
  private double[] y;
  private double[] mark_types;
  private double[] mark_positions;
  private boolean notch_open;
  private boolean curled;

  /**
   * FinTrace Constructor
   * <p/>
   * Comments: Basic constructor method.
   */
  public FinTrace() {
    this.notch_open=false;
    this.curled=false;
  }

  /**
   * FinTrace Constructor
   * <p/>
   * Comments: Basic constructor method.
   * @param int size : size of the tracing arrays, values to be filled in later
   */
  public FinTrace(int size) {
    this.notch_open=false;
    this.curled=false;
    this.x = new double[size];
    this.y = new double[size];
    this.mark_types = new double[size];
    this.mark_positions = new double[size];
  }
  
  /**
   * FinTrace Constructor
   * <p/>
   * Second constructor method with X and Y values provided.
   * @param x double[] : The point location X values
   * @param y double[] : The point location Y values
   * @param mark_types double[] : The type of point at X,Y 
   */
  public FinTrace(double[] x, double y[], double[] mark_types) {
    this.notch_open=false;
    this.curled=false;
    this.x = this.copyarray(x,null);
    this.y = this.copyarray(y,null);
    this.mark_types = this.copyarray(mark_types,null);
    this.mark_positions = new double[x.length];
  }
  
  /**
   * FinTrace Constructor
   * <p/>
   * Comments: Fourth constructor method creating a FinTrace from an 
   * original FinTrace class.
   * @param orig : The original TracePoint to copy the value from. 
   */
  public FinTrace(FinTrace orig) {
    this.notch_open = orig.notch_open;
    this.curled = orig.getCurled();
    this.x = this.copyarray(orig.getX());
    this.y = this.copyarray(orig.getY());
    this.mark_types = this.copyarray(orig.getTypes());
    this.mark_positions = this.copyarray(orig.getPositions());
  }
  
  private double[] copyarray(double[] a){
    return this.copyarray(a, null, false);
  }

  private double[] copyarray(double[] a, double[] r){
    return this.copyarray(a, r, false);
  }

  /**
   * Copies and array, and reverses the values if requested. This is the main work method called by other overloaded methods.
   * @param a double[] : the array to copy
   * @param r double[] : an optional existing return array; if set to null a new array will be created and returned
   * @param reverse boolean : set to true to reverse the array values to return
   * @return double[] : the array copy returned
   */
  private double[] copyarray(double[] a, double[] r, boolean reverse){
    if (r==null || r.length==0){
      r = new double[a.length];
    }
    if (reverse){
      int s=0;
      for (int i=a.length-1;i>=0;i--){
        Array.setDouble(r,s,a[i]);
        s+=1;
      }      
    }else{
      for (int i=0;i<a.length;i++){
        Array.setDouble(r,i,a[i]);
      }
    }
    return r;
  }
  
  /**
   * TracePoint Public Methods
   */  
  
  
  /**
   * Returns a new FinTrace with only mark_type points retained
   * @param mark_type int : The type of mark to retain
   * @return FinTrace : new FinTrace object with the mark_type points retained
   */
  public FinTrace returnMarkType(int mark_type){
    int[] m = {mark_type};
    return this.returnMarkType(m);
  }
  
  /**
   * Returns a new FinTrace with only mark_types points retained
   * @param mark_type int : The type of marks to retain as an array of values
   * @return FinTrace : new FinTrace object with the mark_type points retained
   */
  public FinTrace returnMarkType(int[] mark_types){
    int d = 0;
    int[] m = new int[this.mark_types.length];   
    for (int i=0;i<m.length;i++){
      m[i]=0;
      for (int j=0;j<mark_types.length;j++){
        if (this.mark_types[i]==mark_types[j]){
          m[i]=1;
          d+=1;
          break;
        }
      }
    }
    return this.removeTypes(d,m);    
  }
    
  /**
   * Returns a new FinTrace with the mark_type points removed
   * @param mark_type int : The type of mark to remove
   * @return FinTrace : new FinTrace object with the mark_type points removed
   */
  public FinTrace removeMarkType(int mark_type){
    int[] m = {mark_type};
    return this.removeMarkType(m);
  }
    
  /**
   * Returns a new FinTrace with the mark_type points removed
   * @param mark_type int : The type of mark to remove as an array of values
   * @return FinTrace : new FinTrace object with the mark_type points removed
   */
  public FinTrace removeMarkType(int[] mark_types){
    int d = 0;
    int[] m = new int[this.mark_types.length];   
    for (int i=0;i<m.length;i++){
      m[i]=1;
      for (int j=0;j<mark_types.length;j++){
        if (this.mark_types[i]==mark_types[j]){
          m[i]=0;
          d+=1;
          break;
        }
      }
    }
    return this.removeTypes(d,m);    
  }
  
  /**
   * Remove all parts of the FinTrace based on the array values in m
   * @param size int : Size of the new FinTrace
   * @param m int[] : array of 0/1 values, where a value set to 1 means to keep that records.
   * @return FinTrace : The new reduced FinTrace object
   */
  private FinTrace removeTypes(int size, int[] m){
    FinTrace t = new FinTrace(size);
    int s = 0;
    for (int i=0;i<size;i++){
      if (m[i]==1){
        t.x[s] = this.x[i];
        t.y[s] = this.y[i];
        t.mark_types[s] = this.mark_types[i];
        t.mark_positions[s] = this.mark_positions[i];
        s+=1;
      }
    }
    return t;
  }
  
  /**
   * Appends a FinTrace to an existing fin trace (this) and returns a new FinTrace
   * @param trace FinTrace : the other FinTrace to append
   * @return FinTrace : the new FinTrace to return
   */
  public FinTrace append(FinTrace trace){
    FinTrace ft = new FinTrace(this);
    ft.combine(trace);
    return ft;    
  }
  
  /**
   * Combines a FinTrace to an existing trace (this) in place, thus changing original FinTrace.
   * @param trace FinTrace : the other FinTrace to combine to this
   * @return FinTrace : returns the "this" FinTrace which has been altered
   */
  public FinTrace combine(FinTrace trace){
    double[] fx = new double[x.length+trace.x.length];
    double[] fy = new double[x.length+trace.x.length];
    double[] fm = new double[x.length+trace.x.length];
    double[] fp = new double[x.length+trace.x.length];
    fx = this.copyarray(this.getX(),fx);
    fy = this.copyarray(this.getY(),fy);
    fm = this.copyarray(this.getTypes(),fm);
    fp = this.copyarray(this.getPositions(),fp);
    int s = this.x.length;
    for (int i=0;i<trace.x.length;i++){
      fx[s]=trace.getX(i);
      fy[s]=trace.getY(i);
      fm[s]=trace.getType(i);
      fp[s]=trace.getPosition(i);
      s+=1;
    }
    this.setX(fx);
    this.setY(fy);
    this.setTypes(fm);
    this.setPositions(fp);
    return this;    
  }
  
  /**
   * Reverses the data in order without changing the order of the original instance
   * @return FinTrace : a new FinTrace with data reversed in order
   */
  public FinTrace reverse(){
    FinTrace t = new FinTrace();
    t.x = this.copyarray(this.getX(),null,true);
    t.y = this.copyarray(this.getY(),null,true);
    t.mark_types = this.copyarray(this.getTypes(),null,true);
    t.mark_positions = this.copyarray(this.getPositions(),null,true);
    return t;
  }
  
  /**
   * Simple basic get/set public methods 
   */

  public double[] getX() {
    return x;
  }

  public double getX(int i) {
    return x[i];
  }

  public double[] getY() {
    return y;
  }
  
  public double getY(int i) {
    return y[i];
  }

  public double[] getTypes() {
    return mark_types;
  }

  public double getType(int i) {
    return mark_types[i];
  }

  public double[] getPositions() {
    return mark_positions;
  }
  
  public double getPosition(int i) {
    return mark_positions[i];
  }

  public void setX(double[] x) {
    this.x = x;
  }

  public void setX(int index, double x) {
    this.x[index] = x;
  }
  
  public void setY(double[] y) {
    this.y = y;
  }
  
  public void setY(int index, double y) {
    this.y[index] = y;
  }
  
  public void setTypes(double[] mark_types){
    this.mark_types = mark_types;
  }

  public void setType(int index, double d){
    this.mark_types[index] = d;
  }

  public void setPositions(double[] mark_positions){
    this.mark_positions = mark_positions;
  }
  
  public void setPosition(int index, double mark_positions){
    this.mark_positions[index] = mark_positions;
  }  
  
  public int size(){
    return this.x.length;
  }
  
  public boolean getNotchOpen(){
    return this.notch_open;
  }
  
  public void setNotchOpen(boolean notch_open){
    this.notch_open = notch_open;
  }
  
  public boolean getCurled(){
    return this.curled;
  }
  
  public void setCurled(boolean curled){
    this.curled = curled;
  }  
  
} 

