package ru.vitkud.test;

import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;

public class TestRunner {

	private String suiteName;
	private Class<?> testClass;
	
	protected Shell shell;
	protected Tree tree;

	/**
	 * Launch the application.
	 * @param args
	 */	
	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				System.out.println("Missing Test class name");
				System.exit(2);
			}
			TestRunner window = new TestRunner(args[0]);
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public TestRunner(String suiteName) throws ClassNotFoundException {
		this.suiteName = suiteName;
		testClass = Class.forName(suiteName);
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();

		fillTestSuite();

		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(450, 300);
		shell.setText("JUnit GUI - " + suiteName);
		shell.setLayout(new FormLayout());

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText("&File");

		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);

		MenuItem mntmsaveConfiguration = new MenuItem(menu_1, SWT.NONE);
		mntmsaveConfiguration.setAccelerator(SWT.CTRL | 'S');
		mntmsaveConfiguration.setText("&Save Configuration" + "\tCtrl+S");

		MenuItem mntmRestoresavedconfigurationitem = new MenuItem(menu_1, SWT.NONE);
		mntmRestoresavedconfigurationitem.setText("&Restore Saved Configuration");

		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.setAccelerator(SWT.ALT | 'X');
		mntmExit.setText("E&xit" + "\tAlt+X");
		
		ToolBar toolBar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);
		FormData fd_toolBar = new FormData();
		fd_toolBar.bottom = new FormAttachment(0, 24);
		fd_toolBar.right = new FormAttachment(0, 96);
		fd_toolBar.top = new FormAttachment(0);
		fd_toolBar.left = new FormAttachment(0);
		toolBar.setLayoutData(fd_toolBar);
		
		ToolItem tltmNewItem = new ToolItem(toolBar, SWT.NONE);
		tltmNewItem.setToolTipText("Select &All");
		
		tree = new Tree(shell, SWT.BORDER);
		FormData fd_tree = new FormData();
		fd_tree.bottom = new FormAttachment(100, -10);
		fd_tree.right = new FormAttachment(100, -10);
		fd_tree.top = new FormAttachment(0, 30);
		fd_tree.left = new FormAttachment(0, 10);
		tree.setLayoutData(fd_tree);

	}

	private void fillTestSuite() {

		Request request = Request.aClass(testClass);
		Runner runner = request.getRunner();
		Description rootDescription = runner.getDescription();

		TreeItem rootItem = new TreeItem(tree, 0);
		rootItem.setText(rootDescription.getDisplayName());
		rootItem.setData(rootDescription);
		Deque<TreeItem> stack = new ArrayDeque<>();
		stack.push(rootItem);
		while (stack.size() > 0) {
			TreeItem curItem = stack.pop();
			Description curDescription = (Description) curItem.getData();
			if (curDescription.isSuite()) {
				//TestSuite testSuite = (TestSuite) curTest;
				for (Description childDescripton: curDescription.getChildren()) {
					TreeItem item = new TreeItem(curItem, 0);
					item.setText(childDescripton.toString());
					item.setData(childDescripton);
					stack.push(item);
				}
			}
		}
	}

}
