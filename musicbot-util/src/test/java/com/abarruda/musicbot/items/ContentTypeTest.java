package com.abarruda.musicbot.items;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ContentTypeTest {
	
	@Test
	public void testSoundCloud() {
		assertEquals(ContentType.SOUNDCLOUD, ContentType.determineContentType("http://soundcloud.com/marcmuon/kmc"));
		assertEquals(ContentType.SOUNDCLOUD, ContentType.determineContentType("https://soundcloud.com/marcmuon/kmc"));
		assertEquals(ContentType.SOUNDCLOUD, ContentType.determineContentType("http://www.soundcloud.com/marcmuon/kmc"));
		assertEquals(ContentType.SOUNDCLOUD, ContentType.determineContentType("https://www.soundcloud.com/marcmuon/kmc"));
		assertEquals(ContentType.SOUNDCLOUD, ContentType.determineContentType("http://m.soundcloud.com/marcmuon/kmc"));
		assertEquals(ContentType.SOUNDCLOUD, ContentType.determineContentType("https://m.soundcloud.com/marcmuon/kmc"));
	}
	
	@Test
	public void testYouTube() {
		assertEquals(ContentType.YOUTUBE, ContentType.determineContentType("http://youtube.com/watch?v=8KpSbwsc-38"));
		assertEquals(ContentType.YOUTUBE, ContentType.determineContentType("https://youtube.com/watch?v=8KpSbwsc-38"));
		assertEquals(ContentType.YOUTUBE, ContentType.determineContentType("http://www.youtube.com/watch?v=8KpSbwsc-38"));
		assertEquals(ContentType.YOUTUBE, ContentType.determineContentType("https://www.youtube.com/watch?v=8KpSbwsc-38"));
		assertEquals(ContentType.YOUTUBE, ContentType.determineContentType("http://m.youtube.com/watch?v=8KpSbwsc-38"));
		assertEquals(ContentType.YOUTUBE, ContentType.determineContentType("https://m.youtube.com/watch?v=8KpSbwsc-38"));
	}
	
	@Test
	public void testIsMusicContent() {
		assertTrue(ContentType.isMusicSet(ContentType.SOUNDCLOUD.name()));
		assertTrue(ContentType.isMusicSet(ContentType.YOUTUBE.name()));
		assertFalse(ContentType.isMusicSet(ContentType.MISC.name()));
		assertFalse(ContentType.isMusicSet("Something unsupported"));
		assertFalse(ContentType.isMusicSet(null));
	}

}
