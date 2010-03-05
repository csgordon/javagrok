///////////////////////////////////////////////////////////////////////
// Inferring Aliasing and Ecapsulation Properties for JAVA           //
// Copyright (C) 2007  Kin-Keung Ma, Jeffrey S. Foster               //
//                                                                   //
// This program is free software; you can redistribute it and/or     //
// modify it under the terms of the GNU General Public License       //
// as published by the Free Software Foundation; either version 2    //
// of the License, or (at your option) any later version.            //
//                                                                   //
// This program is distributed in the hope that it will be useful,   //
// but WITHOUT ANY WARRANTY; without even the implied warranty of    //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the     //
// GNU General Public License for more details.                      //
//                                                                   //
// You should have received a copy of the GNU General Public License //
// along with this program; if not, write to the Free Software       //
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA     //
// 02110-1301, USA.                                                  //
///////////////////////////////////////////////////////////////////////

/**
 * 
 */
package uno.analysis;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uno.toolkit.LazySet;



/**
 * @author kkma
 *
 */
public class PropertySets {

	private String name;
	private LazySet RET;
	private LazySet[] PAR;
	private LazySet BAD;
	private LazySet BASE;
	
	public PropertySets(String iname,int pi) {
		name = iname;
		RET = new LazySet(name+".RET");
		BAD = new LazySet(name+".BAD");
		BASE = new LazySet(name+".BASE");
		PAR = new LazySet[pi];
		for(int i=0;i<pi;i++) PAR[i] = new LazySet(name+".PAR"+i);
	}

	/**
	 * @return Returns the bAD.
	 */
	public LazySet getBAD() {
		//this.BAD.remove(null);
		return BAD;
	}

	/**
	 * @return Returns the pAR.
	 */
	public LazySet getPAR(int i) {
		//this.PAR[i].remove(null);
		return PAR[i];
	}

	/**
	 * @return Returns the rET.
	 */
	public LazySet getRET() {
		//this.RET.remove(null);
		return RET;
	}
	
//	public String toString() {
//		String r = "";
//		
//		r += "  RET:\n";
//		for(Iterator i=getRET().iterator();i.hasNext();){
//			r += "    "+i.next().toString() + "\n";
//		}
//		for(int j=0;j<this.PAR.length;j++){
//			r += "  PAR "+j+":\n";
//			for(Iterator i=getPAR(j).iterator();i.hasNext();){
//				r += "    "+i.next().toString() + "\n";
//			}
//		}
//		r += "  BAD:\n";
//		for(Iterator i=getBAD().iterator();i.hasNext();){
//			r += "    "+i.next().toString() + "\n";
//		}
//		return r;
//	}

	public LazySet getBase() {
		// TODO Auto-generated method stub
		//this.BASE.remove(null);
		return BASE;
	}

}
