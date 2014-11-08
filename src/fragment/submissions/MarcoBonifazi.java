package fragment.submissions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MarcoBonifazi {

	public class Trie {
		private HashMap<Character, Trie> children_;
		private HashSet<Integer> stringIds_;
		private Character char_;

		private void init() {
			stringIds_ = new HashSet<Integer>();
			children_ = new HashMap<Character, Trie>();
		}
		public Trie(Character ch, int stringId) {
			init();
			char_ = ch;
			stringIds_.add(stringId);
		}

		public Trie() {
			init();
		}

		private int getStringIdsSize() {
			return stringIds_.size();
		}

		public void copyStringIds(Set<Integer> set) {
			set.addAll(stringIds_);
		}

		public boolean isMyString(int stringId) {
			return stringIds_.contains(stringId);
		}

		private Trie searchChild(Character ch) {
			return children_.get(ch);
		}

		private Trie searchChildNotStringEnding(Character ch, int stringId) {
			Trie trie = children_.get(ch);

			if (trie != null) {
				boolean isSameString = trie.isMyString(stringId);

				if (!isSameString) {
					return trie;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}

		private Trie searchChildNotString(Character ch, int stringId) {
			Trie trie = children_.get(ch);

			if (trie != null) {
				boolean isSameString = trie.isMyString(stringId);

				if (!isSameString || trie.getStringIdsSize() > 1) {
					return trie;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}

		private void replaceStringId(int oldStringId, int newStringId) {
			stringIds_.remove(oldStringId);
			stringIds_.add(newStringId);
		}

		private void addChild(Trie child) {
			Character ch = child.getChar();
			children_.put(ch, child);
		}

		private Character getChar() {
			return char_;
		}
		public void addStringId(int stringId) {
			stringIds_.add(stringId);
		}

		public Map<Character, Trie> getChildren() {
			return children_;
		}

		public void removeStringId(int stringId) {
			stringIds_.remove(stringId);
		}

		public void removeChildWithChar(char c) {
			children_.remove(c);
		}
		public Set<Integer> getStringIds() {
			return stringIds_;
		}
	}

	private int searchLongestPrefix(Trie mainTrie, String string, int stringId, Set<Integer> b) {
		int stringLen = string.length() - 1;

		int i = 0;
		int depth = 0;
		Trie endTrie = null;
		Trie currentTrie = mainTrie;

		while (i <= stringLen) {
			Character c = string.charAt(i);
			Trie child = currentTrie.searchChildNotString(c, stringId);

			endTrie = currentTrie.searchChildNotStringEnding('\0', stringId);
			//System.out.println("C " +currentTrie.getStringIds());
			if (endTrie != null) {
				depth = i;
				b.clear();
				endTrie.copyStringIds(b);
			}

			if (child == null) {
				break;
			} else {
				currentTrie = child;
				i++;
			}
		}

		if (i > stringLen) {
			depth = i;
			if (currentTrie != null) {
				b.clear();
				currentTrie.copyStringIds(b);
			}
		}
		return depth;
	}

	private void addWordInSuffixTrie(Trie currentTrie, String string, int stringId) {

		if (string.length() == 0) {
			Trie child = new Trie('\0', stringId);
			currentTrie.addChild(child);
		} else {
			Character c = string.charAt(0);
			Trie child = new Trie(c, stringId);
			currentTrie.addChild(child);
			addWordInSuffixTrie(child, string.substring(1), stringId);
		}
	}

	private void searchEndingElement(Trie mainTrie, String stringToScan,
			int stringId, String stringToAdd) {
		Trie trie = mainTrie.searchChild('\0');

		if (trie != null) {
			System.out.println("CIAOO");

			if (trie.isMyString(stringId)) {
				if (trie.getStringIdsSize() >= 1) {
					trie.removeStringId(stringId);
					System.out.println("CIAOO");
				} else {
					mainTrie.removeChildWithChar('\0');
				}
				addWordInSuffixTrie(mainTrie, stringToAdd, stringId);
			}
		} else {
			//System.out.println("CIAOO");

			if (stringToScan.length() > 0) {
				//System.out.println(mainTrie.getChar());
				for (Trie child : mainTrie.getChildren().values()) {
					searchEndingElement(child, stringToScan.substring(1), stringId, stringToAdd);
				}
			}
		}
	}

	private void replaceFragmentInSuffixTrie(Trie mainTrie, String string, int oldStringId, int newStringId) {

		if (string.length() >= 0) {
			Character c;
			if (string.length() == 0) {
				c = '\0';
			} else {
				c = string.charAt(0);
			}
			Trie child = mainTrie.searchChild(c);
			//System.out.println(mainTrie.getStringIds() + " " + oldStringId + " " + newStringId);

			mainTrie.replaceStringId(oldStringId, newStringId);

			if (child == null) {
			} else {

				if (string.length() != 0) {
					replaceFragmentInSuffixTrie(child, string.substring(1),  oldStringId, newStringId);
				} else {
					child.replaceStringId(oldStringId, newStringId);
				}
			}
		}
	}


	private void addFragmentInSuffixTrie(Trie mainTrie, String string, int stringId) {

		if (string.length() >= 0) {
			Character c;
			if (string.length() == 0) {
				c = '\0';
			} else {
				c = string.charAt(0);
			}
			Trie child = mainTrie.searchChild(c);

			if (child == null) {
				addWordInSuffixTrie(mainTrie, string, stringId);
			} else {
				child.addStringId(stringId);

				if (string.length() != 0) {
					addFragmentInSuffixTrie(child, string.substring(1), stringId);
				}
			}
		}
	}

	public void printTrie(Trie mainTrie, int level) {
		for (Entry<Character, Trie> tr : mainTrie.children_.entrySet()) {
			Character c = tr.getKey();
			Trie t = tr.getValue();
			String spacing = new String(new char[level]).replace("\0", " ");
			if (c.equals('\0')) {
				System.out.println(spacing + "*" + t.getStringIds());
			} else if (c.equals(' ')){
				System.out.println(spacing + "+" + t.getStringIds());
			} else {
				System.out.println(spacing + c + t.getStringIds());
			}
			printTrie(t, level+1);
		} 

	}

	public void initTrie(String fragment, Trie mainTrie, Map<Integer, String> mapString) {
		String[] parts = {"marco", "marco", "rco", "rcobonifazi", "olemarcobonifaziole"};//fragment.split(";");
		int i = 0;

		for (String s : parts) {
			mapString.put(i,  s);
			i++;
		}
		
		for (Entry<Integer, String> entry : mapString.entrySet()) {
			int ir = entry.getKey();
			String s = entry.getValue();
			//System.out.println("words:" + s);

			for (int len = s.length() - 1; len >= 0; len --) {
				//System.out.println("words:" + s.substring(len));
				addFragmentInSuffixTrie(mainTrie, s.substring(len), ir);
			}
		}
	}
	
	public String run(String fragment) {
		Trie mainTrie = new Trie();
		Map<Integer, String> mapString = new HashMap<Integer, String>();
		
		initTrie(fragment, mainTrie, mapString);
		
		while (mapString.size() > 1) {
			int maxStringId = -1;
			int maxLen = 0;
			Set<Integer> finalSet = null;

			for (Entry<Integer, String> entry : mapString.entrySet()) {
				int stringId = entry.getKey();
				String string = entry.getValue();
				Set<Integer> setStringIds = new HashSet<Integer>();
				int lenSuf = searchLongestPrefix(mainTrie, string, stringId, setStringIds);

				if (lenSuf > maxLen) {
					maxStringId = stringId;
					maxLen = lenSuf;
					finalSet = setStringIds;
				}
			}
			if (maxStringId == -1) {
				break;
			}
			Iterator<Integer> it = finalSet.iterator();
			int stringIdWithGoodSuffix = it.next();

			if (stringIdWithGoodSuffix == maxStringId) {
				stringIdWithGoodSuffix = it.next();
			}

			String toRemoveString = mapString.get(maxStringId);
			String toAddString = toRemoveString.substring(maxLen);
			String toAppendString = mapString.get(stringIdWithGoodSuffix);
			String finalString = toAppendString + toAddString;

			mapString.remove(maxStringId);
			mapString.put(stringIdWithGoodSuffix, finalString);

			for (int len = toRemoveString.length() ; len >= 0; len --) {
				replaceFragmentInSuffixTrie(mainTrie, toRemoveString.substring(len), maxStringId, stringIdWithGoodSuffix);
			}

			if (toAddString.length() > 0) {
				searchEndingElement(mainTrie, toAppendString.substring(toAppendString.length()), stringIdWithGoodSuffix, toAddString);
			}
		}
		if (mapString.size() == 0) {
			return "Error";
		} else {
			return mapString.values().iterator().next();
		}
	}

	static String reassemble(String fragment) {
		MarcoBonifazi mb = new MarcoBonifazi();
		return mb.run(fragment);
	}

	public static void main(String[] args) { 

		try (BufferedReader in = new BufferedReader(new FileReader(args[0]))) { 

			String fragmentProblem; 

			while ((fragmentProblem = in.readLine()) != null) { 
				System.out.println(reassemble(fragmentProblem));
			} 
		} catch (Exception e) { 
			e.printStackTrace(); 
		}
	}
}
