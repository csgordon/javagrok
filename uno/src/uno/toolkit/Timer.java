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
package uno.toolkit;

import java.util.Date;
/**
 * @author kkma
 *
 */
public class Timer {
	private static double[] timer = new double[100];
	private static Date[] start_times = new Date[100];

	public static void reset(){
		timer = new double[100];
		start_times = new Date[100];
	}

	public static void start(int i){
		start_times[i] = new Date();
	}
	public static double get(int i){
		return (timer[i]);
	}
	public static double finish(int i){
		double s = (new Date().getTime() - start_times[i].getTime())/1000.0;
		return (timer[i]+=s);
	}

}
