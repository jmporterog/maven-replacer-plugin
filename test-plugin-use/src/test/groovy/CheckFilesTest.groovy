import org.junit.Test

class CheckFilesTest {
	
	def checkForTokensAndValues = { filename, token="token", value="value" ->
		String content = new File(filename).getText()
		println "Inspecting file: ${filename}"
		
		assert !content.contains(token)
		println "${filename} ${token} removed"
		assert content.contains(value)
		println "${filename} ${value} found"
	}
	
	def checkFileSizeMatches = { filename1, filename2 ->
		assert new File(filename1).size() == new File(filename2).size()
		println "File sizes match between ${filename1} and ${filename2}"
	}

	@Test	
	void shouldHaveFilesReplaced() {
		checkFileSizeMatches "src/main/resources/simple.txt", "target/classes/simple.txt"
		checkFileSizeMatches "src/main/resources/largefile.txt", "target/classes/largefile.txt"
		checkFileSizeMatches "src/main/resources/regex.txt","target/classes/regex.txt"
		
		println "Checking contents"
		new File("target/classes").eachFileMatch(~/.*\.txt/) { checkForTokensAndValues it.getPath() }
		checkForTokensAndValues "target/classes/nesteddir/multiple-files1.txt"
		checkForTokensAndValues "target/classes/nesteddir/multiple-files2.txt"
		checkForTokensAndValues "target/simple-outputfile-remove.txt", "token", ""
		checkForTokensAndValues "target/multiple-tokens-for-map-outputfile.txt", "token1", "value1"
		checkForTokensAndValues "target/multiple-tokens-for-map-outputfile.txt", "token2", "value2"
		checkForTokensAndValues "target/classes/newdir/simple-outputfile.txt"
		checkForTokensAndValues "target/classes/special/multiple-tokens-to-replace.txt", "token1", "value1"
		checkForTokensAndValues "target/classes/special/multiple-tokens-to-replace.txt", "token2", "value2"
		checkForTokensAndValues "target/classes/include2"
		checkForTokensAndValues "target/classes/file1"
		checkForTokensAndValues "target/classes/file2"
		checkForTokensAndValues "target/classes/outdir/simple.txt"
		checkForTokensAndValues "target/classes/linematch.txt", "replace=token", "replace=value"
		checkForTokensAndValues "target/classes/target/outputDir/nesteddir/multiple-files2.txt", "token", "value"
		checkForTokensAndValues "target/outputBasedir/outdir/simple.txt"
	}
}