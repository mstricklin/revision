package edu.utexas.arlut.ciads;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.google.common.collect.*;
import edu.utexas.arlut.ciads.rev.BTree;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    public static void main(String[] args) {
        BTree<Integer,String> bt = new BTree<>(3);


        String s = new String("foo");
        for (int i=0; i<64; i++) {
            bt.put(Integer.valueOf(i), "aaa");
        }

        System.out.println(bt.toString());
        bt.put(Integer.valueOf(60), "bbb");
        System.out.println(bt.toString());

        bt.remove(20);
        System.out.println(bt.toString());


//        bt.stream().forEach(e -> log.info("{}", e));

    }
}

//        bt.put("aaa", "aaa one");
//                bt.put("bbb", "bbb one");
//                bt.put("ccc", "ccc one");
//                bt.put("ddd", "ddd one");
//                bt.put("eee", "eee one");
//                bt.put("fff", "fff one");
//                bt.put("ggg", "ggg one");
//                bt.put("hhh", "hhh one");
//                bt.put("iii", "iii one");
//                bt.put("jjj", "jjj one");
//                bt.put("kkk", "kkk one");
//                bt.put("lll", "lll one");
//                bt.put("mmm", "mmm one");
//                bt.put("nnn", "nnn one");
//                bt.put("ooo", "ooo one");
//                bt.put("ppp", "ppp one");
//                bt.put("qqq", "qqq one");
//                bt.put("rrr", "rrr one");
//                bt.put("sss", "sss one");
//                bt.put("ttt", "ttt one");
//                bt.put("uuu", "uuu one");
//                bt.put("vvv", "vvv one");
//                bt.put("www", "www one");
//                bt.put("xxx", "xxx one");
//                bt.put("yyy", "yyy one");
//                bt.put("zzz", "zzz one");
