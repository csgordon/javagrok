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

package uno.toolkit;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.*;

/*
 * Created on Mar 6, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author kkma
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Msg {
	
	private static PrintWriter out = null;
	private static PrintWriter stdout;
	
	private static int LF_count = 0;
	static{
		try{
			stdout = new PrintWriter(System.out,true);
		}
		catch(Exception e){System.out.println("Error(0)");}
		toStdout();
	}
	
	public static void toFile(String dirs,String name){
		try{
			if(out!=null && out!=stdout) out.close();
			new File(dirs).mkdirs();
			out = new PrintWriter(new FileWriter(dirs+name));
		}
		catch(Exception e){
			//System.out.println("Error(1): "+e);
			try{
				out = new PrintWriter(new FileWriter(dirs+"LONG_FILE_NAME_"+(LF_count++)));
			}catch(Exception ee){out = stdout;}
		}
	}
	public static void toFile(String name){
		try{
			if(out!=null && out!=stdout) out.close();
			out = new PrintWriter(new FileWriter(name));
		}
		catch(Exception e){System.out.println("Error(1)");}
	}
	
	public static void toStdout(){
		try{
			if(out!=null && out!=stdout) out.close();
			out = stdout;
		}
		catch(Exception e){System.out.println("Error(2)");}
	}
	
	/**
	 * @return
	 */
	public static boolean checkError() {
		return out.checkError();
	}
	/**
	 * @throws java.io.IOException
	 */
	public static void close() throws IOException {
		out.close();
	}

	/**
	 * @throws java.io.IOException
	 */
	public static void flush() throws IOException {
		out.flush();
	}

	/**
	 * @param arg0
	 */
	public static void print(boolean arg0) {
		out.print(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void print(char arg0) {
		out.print(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void print(char[] arg0) {
		out.print(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void print(double arg0) {
		out.print(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void print(float arg0) {
		out.print(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void print(int arg0) {
		out.print(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void print(Object arg0) {
		out.print(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void print(String arg0) {
		out.print(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void print(long arg0) {
		out.print(arg0);
	}
	/**
	 * 
	 */
	public static void println() {
		out.println();
	}
	/**
	 * @param arg0
	 */
	public static void println(boolean arg0) {
		out.println(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void println(char arg0) {
		out.println(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void println(char[] arg0) {
		out.println(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void println(double arg0) {
		out.println(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void println(float arg0) {
		out.println(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void println(int arg0) {
		out.println(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void println(Object arg0) {
		out.println(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void println(String arg0) {
		out.println(arg0);
	}
	/**
	 * @param arg0
	 */
	public static void println(long arg0) {
		out.println(arg0);
	}

	/**
	 * @param arg0
	 * @throws java.io.IOException
	 */
	public static void write(char[] arg0) throws IOException {
		out.write(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @throws java.io.IOException
	 */
	public static void write(char[] arg0, int arg1, int arg2) throws IOException {
		out.write(arg0, arg1, arg2);
	}
	/**
	 * @param arg0
	 * @throws java.io.IOException
	 */
	public static void write(int arg0) throws IOException {
		out.write(arg0);
	}
	/**
	 * @param arg0
	 * @throws java.io.IOException
	 */
	public static void write(String arg0) throws IOException {
		out.write(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @throws java.io.IOException
	 */
	public static void write(String arg0, int arg1, int arg2) throws IOException {
		out.write(arg0, arg1, arg2);
	}
}
