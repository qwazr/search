/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.server;

import org.junit.Assert;
import org.junit.Test;

import java.net.URISyntaxException;

public class RemoteServiceTest {

    @Test
    public void builderAndEquality() throws URISyntaxException {
        RemoteService rs1 = RemoteService.of()
                .setHost("birdie")
                .setPort(9091)
                .setTimeout(1234)
                .setPath("avatar")
                .setUsername("jake")
                .setPassword("suly")
                .build();
        Assert.assertNotNull(rs1);

        RemoteService rs2 = RemoteService.of("http://jake:suly@birdie:9091/avatar").setTimeout(1234).build();
        Assert.assertEquals(rs1, rs2);

        RemoteService rs3 = RemoteService.of("http://jake:suly@birdie:9091/avatar?timeout=1234").build();
        Assert.assertEquals(rs1, rs3);

        RemoteService rsA = RemoteService.of("http://jake:suly@birdie:9091/avatar?timeout=1235").build();
        Assert.assertNotEquals(rs1, rsA);

        RemoteService rsB = RemoteService.of("http://jake:suly@birdie:9092/avatar?timeout=1234").build();
        Assert.assertNotEquals(rs1, rsB);

        RemoteService rsC = RemoteService.of("http://jake:suly@example:9091/avatar?timeout=1234").build();
        Assert.assertNotEquals(rs1, rsC);

    }

}
