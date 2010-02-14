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

package uno.test;

interface Subject {
    void setData(int d);
}
class ConcreteSubject implements Subject {
    private int data;
    public void setData(int d) { data = d; }
}
class Factory {
    public Subject getSubject() {       // returns unique
        Subject r = new ConcreteSubject();
        Subject s = r;
        return r;
    }
}
class Proxy implements Subject {
    private Subject s;                   // owned by this
    public Proxy(Subject s) {
        this.s = s;
    }
    public void setData(int d) {
        if(s!=null) s.setData(d*d);
    }
}
class Main {
    public void main(Factory f) {
        Subject s = f.getSubject();
        s.setData(1);                    // uses s directly
        Proxy proxy = new Proxy(s);        // proxy owns s
        proxy.setData(2);      // s now used through proxy
    }
}
