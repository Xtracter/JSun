// Copyright (c) 2023 CrazedoutSoft / Fredrik Roos
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is furnished
// to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
// INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
// PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
// OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// info@crazedout.com
package com.crazedout.jsun;

import javax.script.ScriptException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class JSunResult {

    private final List<JSun> list = new LinkedList<>();
    private boolean closeRs = true;

    public JSunResult(Connection con, String sql) throws SQLException {
        this.execute(executeQuery(con,sql));
    }

    public JSunResult(ResultSet rs) throws SQLException {
        this.execute(rs);
    }

    private void execute(ResultSet rs) throws SQLException {
        this.executeResultSet(rs);
    }

    private ResultSet executeQuery(Connection con, String sql) throws SQLException {
        return con.createStatement().executeQuery(sql);
    }

    private void executeResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();

        while(rs.next()){
            List<Object> oList = new LinkedList<>();
            for(int i = 0; i < meta.getColumnCount(); i++){
                String name = meta.getColumnName(i+1);
                Object value = rs.getObject(name);
                oList.add(value);
            }
            list.add(new JSun(oList));
        }
        if(closeRs) rs.close();
    }

    public void closeResultSetWhenDone(boolean close){
        this.closeRs=close;
    }

    public List<JSun> getResultList(){
        return this.list;
    }

    @Override
    public String toString(){
        String str = super.toString();
        try {
            str = (new JSun(list)).eval();
        }catch(ScriptException ex){
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
        return str;
    }
}






