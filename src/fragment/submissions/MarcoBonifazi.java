package fragment.submissions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class MarcoBonifazi {
	private String fragment_;
	
	public class Trie {
		private HashMap<Character, Trie> children_;
		private HashSet<Integer> stringIds_;
		private Character char_;
		
		public Trie(Character ch, int stringId) {
			stringIds_ = new HashSet<Integer>();
			char_ = ch;
			stringIds_.add(stringId);
			children_ = new HashMap<Character, Trie>();
		}
		
		public Set<Integer> getStringIds() {
			return stringIds_;
		}
		public boolean isMyString(int stringId) {
			return stringIds_.contains(stringId);
		}
		
		public Trie() {
			stringIds_ = new HashSet<Integer>();
			children_ = new HashMap<Character, Trie>();
		}
		
		private Trie searchChild(Character ch) {
			return children_.get(ch);
		}
		
		private Trie searchChildNotString2(Character ch, int stringId) {
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
				
				if (!isSameString || trie.getStringIds().size() > 1) {
					return trie;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}

		private void replaceChild(int oldStringId, int newStringId) {
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
	}

	private int searchLongestPrefix(Trie mainTrie, String string, int stringId, Set<Integer> b) {
		int stringLen = string.length() - 1;
		
		int i = 0;
		int depth = 0;
		Trie temp = null;
		Trie currentTrie = mainTrie;
		
		while (i <= stringLen) {
			Character c = string.charAt(i);
			Trie child = currentTrie.searchChildNotString(c, stringId);
			
			temp = currentTrie.searchChildNotString2('\0', stringId);
			//System.out.println("C " +currentTrie.getStringIds());
			if (temp != null) {
				depth = i;
				b.clear();
				b.addAll(temp.getStringIds());
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
				b.addAll(currentTrie.getStringIds());
			}
		}
	
		return depth;

	}

	private void addSuffixInTrie(Trie currentTrie, String string, int stringId) {
		//System.out.println("W " +string.length() + " " + string);

		if (string.length() == 0) {
			Trie child = new Trie('\0', stringId);
			currentTrie.addChild(child);
		} else {
			Character c = string.charAt(0);
			Trie child = new Trie(c, stringId);
			currentTrie.addChild(child);
			addSuffixInTrie(child, string.substring(1), stringId);
		}
	}
	
	private void replaceStringsId(Trie mainTrie, String string, int oldStringId, int newStringId) {
		for (int len = string.length() - 1; len >= 0; len --) {
			replaceFragmentInSuffixTrie(mainTrie, string.substring(len), oldStringId, newStringId);
		}
	}
	
	private void replaceEndingStringsId(Trie mainTrie, String string, String stringToAdd, int stringId) {
		for (int len = string.length() - 1; len >= 0; len --) {
			searchEndingElement(mainTrie, string.substring(len), stringId, stringToAdd);
		}
	}

	
	private void searchEndingElement(Trie mainTrie, String stringToScan,
			int stringId, String stringToAdd) {
		Trie trie = mainTrie.searchChild('\0');
		
		if (trie != null) {
			if (trie.isMyString(stringId)) {
				if (trie.getStringIds().size() > 1) {
					trie.removeStringId(stringId);
				} else {
					mainTrie.removeChildWithChar('\0');
				}
				addSuffixInTrie(mainTrie, stringToAdd, stringId);
			}
		} else {
			for (Trie child : mainTrie.getChildren().values()) {
				searchEndingElement(child, stringToScan.substring(1), stringId, stringToAdd);
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

			if (child == null) {
				mainTrie.replaceChild(oldStringId, newStringId);
			} else {
				mainTrie.replaceChild(oldStringId, newStringId);
				
				if (string.length() != 0) {
					replaceFragmentInSuffixTrie(child, string.substring(1),  oldStringId, newStringId);
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
				addSuffixInTrie(mainTrie, string, stringId);
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
	
	public void run() {

		String[] parts = fragment_.split(";");//{"bamama", "am", "amara", "caba"};//fragment_.split(";");//
		int i = 0;

		//String[] parts = {"ciao"};
		Map<Integer, String> mapString = new TreeMap<Integer, String>();
		for (String s : parts) {
			mapString.put(i,  s);
			i++;
		}

		int re = 0;
		while (true) {
			Trie mainTrie = new Trie();

			for (Entry<Integer, String> entry : mapString.entrySet()) {
				int ir = entry.getKey();
				String s = entry.getValue();
				//System.out.println("words:" + s);

				for (int len = s.length() - 1; len >= 0; len --) {
					//System.out.println("words:" + s.substring(len));
					addFragmentInSuffixTrie(mainTrie, s.substring(len), ir);
				}
			}
			//printTrie(mainTrie, 0);

			int maxStringId = -1;
			int maxLen = 0;
			HashSet<Integer> finalSet = new HashSet<Integer>();

			for (Entry<Integer, String> entry : mapString.entrySet()) {
				int ir = entry.getKey();
				String s = entry.getValue();
				HashSet<Integer> b = new HashSet<Integer>();
				int lenSuf = searchLongestPrefix(mainTrie, s, ir, b);
				System.out.println(ir + " " + s + " " + lenSuf + " " + b); 

				if (lenSuf > maxLen) {
					maxStringId = ir;
					maxLen = lenSuf;
					finalSet.clear();
					finalSet.addAll(b);
				}
				i++;
			}
			if (maxStringId == -1) {
				break;
			}
			Iterator<Integer> it = finalSet.iterator();
			int stringIdWithGoodSuffix = it.next();
			System.out.println("stringIdWithGoodSuffix: " + stringIdWithGoodSuffix);

			if (stringIdWithGoodSuffix == maxStringId) {
				stringIdWithGoodSuffix = it.next();//(stringIdWithGoodSuffix+ 1);
				System.out.println("stringIdWithGoodSuffix: " + stringIdWithGoodSuffix);

			}
			String finalString = mapString.get(stringIdWithGoodSuffix) + mapString.get(maxStringId).substring(maxLen);
			System.out.println("Max: " + "id:" +  maxStringId + " len:" + maxLen + " " + "replId:"+ stringIdWithGoodSuffix + " " + finalString); 

			mapString.remove(maxStringId);
			mapString.put(stringIdWithGoodSuffix, finalString);
			re++;
		}
		for (Entry<Integer, String> entry : mapString.entrySet()) {
			int ir = entry.getKey();
			String s = entry.getValue();
			System.out.println("R: " +s); 

		}

	}
	
	public MarcoBonifazi(String fragment) {
		fragment_ = fragment;
	};

	static String reassemble(String fragment) {
		MarcoBonifazi mb = new MarcoBonifazi(fragment);
		mb.run();
		return "";
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


