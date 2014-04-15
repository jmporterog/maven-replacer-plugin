package com.google.code.maven_replacer_plugin;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.code.maven_replacer_plugin.file.FileUtils;

@RunWith(MockitoJUnitRunner.class)
public class TokenValueMapFactoryTest {
	private static final String FILENAME = "some file";
	private static final boolean COMMENTS_ENABLED = true;
	private static final boolean COMMENTS_DISABLED = false;

	@Mock
	private FileUtils fileUtils;

	private TokenValueMapFactory factory;

	@Before
	public void setUp() {
		factory = new TokenValueMapFactory(fileUtils);
	}
	
	@Test
	public void shouldReturnContextsFromFile() throws Exception {
		when(fileUtils.readFile(FILENAME)).thenReturn("token=value");
		
		List<Replacement> contexts = factory.contextsForFile(FILENAME, COMMENTS_DISABLED, false);
		assertThat(contexts, notNullValue());
		assertThat(contexts.size(), is(1));
		assertThat(contexts.get(0).getToken(), equalTo("token"));
		assertThat(contexts.get(0).getValue(), equalTo("value"));
	}

	@Test
	public void shouldReturnContextsFromFileAndIgnoreBlankLinesAndComments() throws Exception {
		when(fileUtils.readFile(FILENAME)).thenReturn("\n  \ntoken1=value1\ntoken2 = value2\n#some comment\n");
		
		List<Replacement> contexts = factory.contextsForFile(FILENAME, COMMENTS_ENABLED, false);
		assertThat(contexts, notNullValue());
		assertThat(contexts.size(), is(2));
		assertThat(contexts.get(0).getToken(), equalTo("token1"));
		assertThat(contexts.get(0).getValue(), equalTo("value1"));
		assertThat(contexts.get(1).getToken(), equalTo("token2"));
		assertThat(contexts.get(1).getValue(), equalTo("value2"));
	}
	
	@Test
	public void shouldReturnContextsFromFileAndIgnoreBlankLinesUsingCommentLinesIfCommentsDisabled() throws Exception {
		when(fileUtils.readFile(FILENAME)).thenReturn("\n  \ntoken1=value1\ntoken2=value2\n#some=#comment\n");
		
		List<Replacement> contexts = factory.contextsForFile(FILENAME, COMMENTS_DISABLED, false);
		assertThat(contexts, notNullValue());
		assertThat(contexts.size(), is(3));
		assertThat(contexts.get(0).getToken(), equalTo("token1"));
		assertThat(contexts.get(0).getValue(), equalTo("value1"));
		assertThat(contexts.get(1).getToken(), equalTo("token2"));
		assertThat(contexts.get(1).getValue(), equalTo("value2"));
		assertThat(contexts.get(2).getToken(), equalTo("#some"));
		assertThat(contexts.get(2).getValue(), equalTo("#comment"));
	}
	
	@Test
	public void shouldIgnoreTokensWithNoSeparatedValue() throws Exception {
		when(fileUtils.readFile(FILENAME)).thenReturn("#comment\ntoken2");
		List<Replacement> contexts = factory.contextsForFile(FILENAME, COMMENTS_DISABLED, false);
		assertThat(contexts, notNullValue());
		assertTrue(contexts.isEmpty());
	}
	
	@Test
	public void shouldReturnRegexContextsFromFile() throws Exception {
		when(fileUtils.readFile(FILENAME)).thenReturn("\\=tok\\=en1=val\\=ue1\nto$ke..n2=value2");
		
		List<Replacement> contexts = factory.contextsForFile(FILENAME, COMMENTS_ENABLED, false);
		assertThat(contexts, notNullValue());
		assertThat(contexts.size(), is(2));
		assertThat(contexts.get(0).getToken(), equalTo("\\=tok\\=en1"));
		assertThat(contexts.get(0).getValue(), equalTo("val\\=ue1"));
		assertThat(contexts.get(1).getToken(), equalTo("to$ke..n2"));
		assertThat(contexts.get(1).getValue(), equalTo("value2"));
	}
	
	@Test
	public void shouldReturnRegexContextsFromFileUnescaping() throws Exception {
		when(fileUtils.readFile(FILENAME)).thenReturn("\\\\=tok\\\\=en1=val\\\\=ue1\nto$ke..n2=value2");
		
		List<Replacement> contexts = factory.contextsForFile(FILENAME, COMMENTS_ENABLED, true);
		assertThat(contexts, notNullValue());
		assertThat(contexts.size(), is(2));
		assertThat(contexts.get(0).getToken(), equalTo("\\=tok\\=en1"));
		assertThat(contexts.get(0).getValue(), equalTo("val\\=ue1"));
		assertThat(contexts.get(1).getToken(), equalTo("to$ke..n2"));
		assertThat(contexts.get(1).getValue(), equalTo("value2"));
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfNoTokenForValue() throws Exception {
		when(fileUtils.readFile(FILENAME)).thenReturn("=value");
		factory.contextsForFile(FILENAME, COMMENTS_DISABLED, false);
	}
	
	@Test
	public void shouldSupportEmptyFileAndReturnNoReplacements() throws Exception {
		when(fileUtils.readFile(FILENAME)).thenReturn("");
		List<Replacement> contexts = factory.contextsForFile(FILENAME, COMMENTS_DISABLED, false);
		assertThat(contexts, notNullValue());
		assertTrue(contexts.isEmpty());
	}
	
	@Test
	public void shouldReturnListOfContextsFromVariable() {
		List<Replacement> contexts = factory.contextsForVariable("#comment,token1=value1,token2=value2", true, false);
		assertThat(contexts, notNullValue());
		assertThat(contexts.size(), is(2));
		assertThat(contexts, hasItem(contextWith("token1", "value1")));
		assertThat(contexts, hasItem(contextWith("token2", "value2")));
	}

	private Matcher<Replacement> contextWith(final String token, final String value) {
		return new BaseMatcher<Replacement>() {
			public boolean matches(Object o) {
				Replacement replacement = (Replacement)o;
				return token.equals(replacement.getToken()) && value.equals(replacement.getValue());
			}

			public void describeTo(Description desc) {
				desc.appendText("token=" + token + ", value=" + value);
			}
		};
	}
}