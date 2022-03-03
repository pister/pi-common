package com.github.pister.common.lang;

import com.github.pister.common.lang.codec.Base33;
import junit.framework.TestCase;

/**
 * Created by songlihuang on 2019/1/26.
 */
public class Base33Test extends TestCase {

    public void test1() throws Exception {
        String base = "a";
        for (char i = 'a'; i <= 'z'; i++) {
            base = base + i;
            String out = new String(Base33.encode(base.getBytes()));
            System.out.println(out);
            byte[] raw = Base33.decode(out.getBytes());
            System.out.println(new String(raw));
        }

    }

}