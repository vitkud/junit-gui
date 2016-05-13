package ru.vitkud.test;

import java.beans.Beans;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

public class TestRunner extends RunListener {

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
			System.exit(runTest(Class.forName(args[0])));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static int runTest(Class<?> test) {
		TestRunner window = new TestRunner();
		window.setSuite(test);
		window.open();
		return window.getfFailureCount();
	}

	private static final String TEST_INI_FILE = "junit-gui.properties";
	private static final String CN_CONFIG_INI_SECTION = "Config";

	protected Display display;
	protected Shell shell;
	private ToolBar toolBar;
	private Tree testTree;
	private Table tableResults;
	private Table tableFailureList;
	private MenuItem mntmSelectAll;
	private MenuItem mntmDeselectAll;
	private MenuItem mntmSelectFailed;
	private MenuItem mntmSelectCurrent;
	private MenuItem mntmDeselectCurrent;
	private MenuItem mntmHideTestNodes;
	private MenuItem mntmExpandAll;
	private MenuItem mntmAutoSave;
	private MenuItem mntmErrorBoxVisible;
	private MenuItem mntmHideTestNodesOnOpen;
	private MenuItem mntmShowTestedNode;
	private MenuItem mntmBreakOnFailures;
	private MenuItem mntmUseRegistry;
	private MenuItem mntmWarnIfFailTestOverridden;
	private MenuItem mntmFailTestCaseIfNoChecksExecuted;
	private MenuItem mntmReportMemoryLeakTypeOnShutdown;
	private MenuItem mntmFailTestCaseIfMemoryLeaked;
	private MenuItem mntmIgnoreMemoryLeakInSetUpTearDown;
	private ToolItem tltmSelectAll;
	private ToolItem tltmDeselectAll;
	private ToolItem tltmSelectFailed;
	private ToolItem tltmSelectCurrent;
	private ToolItem tltmDeselectCurrent;
	private SashForm sashForm;
	private Composite compositeResults;
	private ProgressBar progressBar;
	private ProgressBar scoreBar;
	private Label lblProgressPercent;
	private Composite compositeErrorBox;
	private StyledText errorMessageStyledText;
	
	// Color constants for the progress bar and failure details panel
	private Color clOk;// = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
	private Color clFailure;// = Display.getCurrent().getSystemColor(SWT.COLOR_MAGENTA);
	private Color clError;// = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

	// color images used in the test tree and failure list
	private Image imgNone;
	private Image imgRunning;
	private Image imgRun;
	private Image imgHasProps;
	private Image imgFailed;
	private Image imgError;
	private Map<Image, Integer>	indexesImages; 

	public TestRunner() {
		display = Display.getDefault();
		createContents();

		if (!Beans.isDesignTime()) {
			formCreate();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	// XXX @Deprecated
	public void processMessages() {
		if (shell.isDisposed())
			throw new SWTException("Application is closed");
		else
			while (display.readAndDispatch());
	}

	private List<TreeItem> getAllTestTreeItems() {
		List<TreeItem> result = new ArrayList<>(Arrays.asList(testTree.getItems()));
		for (int i = 0; i < result.size(); ++i) {
			Collections.addAll(result, result.get(i).getItems());
		}
		return result;
	}
	
	private void formCreate() {
		fTests = new ArrayList<>();
		testToNodeMap = new HashMap<>();
		loadConfiguration();

		setUpStateImages();
		setupCustomShortcuts();
		testTree.removeAll(); // XXX
		enableUI(false);
		clearFailureMessage();
		setup();

		mntmFailTestCaseIfMemoryLeaked.setEnabled(false);
		mntmReportMemoryLeakTypeOnShutdown.setSelection(false);
		mntmReportMemoryLeakTypeOnShutdown.setEnabled(false);
		
		if (!mntmFailTestCaseIfMemoryLeaked.isEnabled())
			mntmFailTestCaseIfMemoryLeaked.setSelection(false);
		mntmIgnoreMemoryLeakInSetUpTearDown.setEnabled(mntmFailTestCaseIfMemoryLeaked.getSelection());
		if (!mntmIgnoreMemoryLeakInSetUpTearDown.getEnabled())
			mntmIgnoreMemoryLeakInSetUpTearDown.setSelection(false);
	}

	private void formDestroy() {
		// TODO ...
		autoSaveConfiguration();
		// TODO ...
	}
	
	private void formShow() {
		setupGuiNodes();
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellActivated(ShellEvent e) {
				formShow();
			}
			@Override
			public void shellClosed(ShellEvent e) {
				formDestroy();
			}
		});
		shell.setMinimumSize(new Point(300, 200));
		shell.setSize(500, 500);
		shell.setText("JUnit GUI");
		FormLayout fl_shell = new FormLayout();
		fl_shell.marginBottom = 1;
		fl_shell.marginRight = 4;
		fl_shell.marginLeft = 4;
		shell.setLayout(fl_shell);

		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText("&File");

		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);

		MenuItem mntmSaveConfiguration = new MenuItem(menu_1, SWT.NONE);
		mntmSaveConfiguration.setAccelerator(SWT.CTRL | 'S');
		mntmSaveConfiguration.setText("&Save Configuration" + "\tCtrl+S");

		MenuItem mntmRestoreSavedConfiguration = new MenuItem(menu_1, SWT.NONE);
		mntmRestoreSavedConfiguration.setText("&Restore Saved Configuration");

		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/6.png"));
		mntmExit.setAccelerator(SWT.ALT | 'X');
		mntmExit.setText("E&xit" + "\tAlt+X");
		
		MenuItem mntmTestTree = new MenuItem(menu, SWT.CASCADE);
		mntmTestTree.setText("Test &Tree");
		
		Menu menu_2 = new Menu(mntmTestTree);
		mntmTestTree.setMenu(menu_2);
		
		mntmSelectAll = new MenuItem(menu_2, SWT.NONE);
		mntmSelectAll.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/0.png"));
		mntmSelectAll.setAccelerator(SWT.CTRL | SWT.ALT | 'A');
		mntmSelectAll.setText("Select &All" + "\tCtrl+Alt+A");
		
		mntmDeselectAll = new MenuItem(menu_2, SWT.NONE);
		mntmDeselectAll.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/1.png"));
		mntmDeselectAll.setAccelerator(SWT.CTRL | SWT.DEL);
		mntmDeselectAll.setText("&Deselect All" + "\tCtrl+Del");
		
		mntmSelectFailed = new MenuItem(menu_2, SWT.NONE);
		mntmSelectFailed.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/4.png"));
		mntmSelectFailed.setAccelerator(SWT.CTRL | 'F');
		mntmSelectFailed.setText("Select Fai&led" + "\tCtrl+F");
		
		mntmSelectCurrent = new MenuItem(menu_2, SWT.NONE);
		mntmSelectCurrent.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/2.png"));
		mntmSelectCurrent.setAccelerator(SWT.SHIFT | SWT.CTRL | 'A');
		mntmSelectCurrent.setText("Select &Current" + "\tShift+Ctrl+A");
		
		mntmDeselectCurrent = new MenuItem(menu_2, SWT.NONE);
		mntmDeselectCurrent.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/3.png"));
		mntmDeselectCurrent.setAccelerator(SWT.SHIFT | SWT.CTRL | SWT.DEL);
		mntmDeselectCurrent.setText("Deselect C&urrent" + "\tShift+Ctrl+Del");
		
		new MenuItem(menu_2, SWT.SEPARATOR);
		
		mntmHideTestNodes = new MenuItem(menu_2, SWT.NONE);
		mntmHideTestNodes.setAccelerator(SWT.CTRL | 'H');
		mntmHideTestNodes.setText("&Hide Test Nodes" + "\tCtrl+H");
		
		mntmExpandAll = new MenuItem(menu_2, SWT.NONE);
		mntmExpandAll.setAccelerator(SWT.CTRL | 'P');
		mntmExpandAll.setText("Ex&pand All" + "\tCtrl+P");
		
		new MenuItem(menu_2, SWT.SEPARATOR);

		MenuItem mntmGoToNextSelectedNode = new MenuItem(menu_2, SWT.NONE);
		mntmGoToNextSelectedNode.setAccelerator(SWT.ALT | SWT.DOWN);
		mntmGoToNextSelectedNode.setText("Go To Next Selected Test" + "\tAlt+Down");
		
		MenuItem mntmGoToPreviousSelectedNode = new MenuItem(menu_2, SWT.NONE);
		mntmGoToPreviousSelectedNode.setAccelerator(SWT.ALT | SWT.UP);
		mntmGoToPreviousSelectedNode.setText("Go To Previous Selected Test" + "\tAlt+Up");
		
		new MenuItem(menu_2, SWT.SEPARATOR);

		MenuItem mntmCopytestnametoclipboard = new MenuItem(menu_2, SWT.NONE);
		mntmCopytestnametoclipboard.setAccelerator(SWT.CTRL | SWT.ALT | 'C');
		mntmCopytestnametoclipboard.setText("Copy testname to clipboard" + "\tCtrl+Alt+C");
		
		new MenuItem(menu_2, SWT.SEPARATOR);

		MenuItem mntmRunSelectedTest = new MenuItem(menu_2, SWT.NONE);
		mntmRunSelectedTest.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/10.png"));
		mntmRunSelectedTest.setAccelerator(SWT.F8);
		mntmRunSelectedTest.setText("Run selected test" + "\tF8");
		
		MenuItem mntmOptions = new MenuItem(menu, SWT.CASCADE);
		mntmOptions.setText("&Options");
		
		Menu menu_3 = new Menu(mntmOptions);
		mntmOptions.setMenu(menu_3);
		
		mntmAutoSave = new MenuItem(menu_3, SWT.CHECK);
		mntmAutoSave.setSelection(true);
		mntmAutoSave.setText("&Auto Save Configuration");
		
		mntmErrorBoxVisible = new MenuItem(menu_3, SWT.CHECK);
		mntmErrorBoxVisible.setSelection(true);
		mntmErrorBoxVisible.setText("&Error Box Visible");
		
		MenuItem mntmAutoChangeFocus = new MenuItem(menu_3, SWT.CHECK);
		mntmAutoChangeFocus.setSelection(true);
		mntmAutoChangeFocus.setText("Auto Change &Focus");
		
		mntmHideTestNodesOnOpen = new MenuItem(menu_3, SWT.CHECK);
		mntmHideTestNodesOnOpen.setText("&Hide Test Nodes On Open");
		
		mntmShowTestedNode = new MenuItem(menu_3, SWT.CHECK);
		mntmShowTestedNode.setSelection(true);
		mntmShowTestedNode.setText("&Show Tested Node");
		
		mntmBreakOnFailures = new MenuItem(menu_3, SWT.CHECK);
		mntmBreakOnFailures.setText("&Break on Failures");
		
		mntmUseRegistry = new MenuItem(menu_3, SWT.CHECK);
		mntmUseRegistry.setText("Use Registry");
		
		new MenuItem(menu_3, SWT.SEPARATOR);

		MenuItem mntmShowTestCasesWithRunTimeProperties = new MenuItem(menu_3, SWT.CHECK);
		mntmShowTestCasesWithRunTimeProperties.setText("Show TestCases with RunTime Properties");
		
		mntmWarnIfFailTestOverridden = new MenuItem(menu_3, SWT.CHECK);
		mntmWarnIfFailTestOverridden.setText("Warn if Fail Test Overridden");
		
		new MenuItem(menu_3, SWT.SEPARATOR);

		mntmFailTestCaseIfNoChecksExecuted = new MenuItem(menu_3, SWT.CHECK);
		mntmFailTestCaseIfNoChecksExecuted.setText("Fail TestCase if no checks executed");
		
		new MenuItem(menu_3, SWT.SEPARATOR);

		mntmReportMemoryLeakTypeOnShutdown = new MenuItem(menu_3, SWT.CHECK);
		mntmReportMemoryLeakTypeOnShutdown.setText("Report memory leak type on Shutdown");
		
		mntmFailTestCaseIfMemoryLeaked = new MenuItem(menu_3, SWT.CHECK);
		mntmFailTestCaseIfMemoryLeaked.setText("Fail TestCase if memory leaked");
		
		mntmIgnoreMemoryLeakInSetUpTearDown = new MenuItem(menu_3, SWT.CHECK);
		mntmIgnoreMemoryLeakInSetUpTearDown.setEnabled(false);
		mntmIgnoreMemoryLeakInSetUpTearDown.setText("Ignore memory leak in SetUp/TearDown");
		
		MenuItem mntmActions = new MenuItem(menu, SWT.CASCADE);
		mntmActions.setText("Actio&ns");
		
		Menu menu_4 = new Menu(mntmActions);
		mntmActions.setMenu(menu_4);
		
		MenuItem mntmRun = new MenuItem(menu_4, SWT.NONE);
		mntmRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runActionExecute();
			}
		});
		mntmRun.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/7.png"));
		mntmRun.setAccelerator(SWT.F9);
		mntmRun.setText("Run" + "\tF9");
		
		MenuItem mntmRunSelectedTest2 = new MenuItem(menu_4, SWT.NONE);
		mntmRunSelectedTest2.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/10.png"));
		mntmRunSelectedTest2.setAccelerator(SWT.F8);
		mntmRunSelectedTest2.setText("Run selected test" + "\tF8");
		
		MenuItem mntmStop = new MenuItem(menu_4, SWT.NONE);
		mntmStop.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/8.png"));
		mntmStop.setAccelerator(SWT.CTRL | SWT.F2);
		mntmStop.setText("&Stop" + "\tCtrl+F2");
		
		MenuItem mntmCopyErrorMessageToClipboard = new MenuItem(menu_4, SWT.NONE);
		mntmCopyErrorMessageToClipboard.setEnabled(false);
		mntmCopyErrorMessageToClipboard.setAccelerator(SWT.SHIFT | SWT.CTRL | 'C');
		mntmCopyErrorMessageToClipboard.setText("&Copy Error Message to Clipboard" + "\tShift+Ctrl+C");
		
		Label menuSeparator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_menuSeparator = new FormData();
		fd_menuSeparator.right = new FormAttachment(100);
		fd_menuSeparator.left = new FormAttachment(0);
		menuSeparator.setLayoutData(fd_menuSeparator);
		
		toolBar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);
		FormData fd_toolBar = new FormData();
		fd_toolBar.top = new FormAttachment(menuSeparator);
		fd_toolBar.right = new FormAttachment(100);
		fd_toolBar.left = new FormAttachment(0);
		toolBar.setLayoutData(fd_toolBar);
		
		tltmSelectAll = new ToolItem(toolBar, SWT.NONE);
		tltmSelectAll.setToolTipText("Select all tests");
		tltmSelectAll.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/0.png"));
		
		tltmDeselectAll = new ToolItem(toolBar, SWT.NONE);
		tltmDeselectAll.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/1.png"));
		tltmDeselectAll.setToolTipText("Deselect all tests");
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		tltmSelectFailed = new ToolItem(toolBar, SWT.NONE);
		tltmSelectFailed.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/4.png"));
		tltmSelectFailed.setToolTipText("Select all failed tests");
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		tltmSelectCurrent = new ToolItem(toolBar, SWT.NONE);
		tltmSelectCurrent.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/2.png"));
		tltmSelectCurrent.setToolTipText("Select current test");
		
		tltmDeselectCurrent = new ToolItem(toolBar, SWT.NONE);
		tltmDeselectCurrent.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/3.png"));
		tltmDeselectCurrent.setToolTipText("Deselect current test");
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		ToolItem tltmRun = new ToolItem(toolBar, SWT.NONE);
		tltmRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runActionExecute();
			}
		});
		tltmRun.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/7.png"));
		tltmRun.setToolTipText("Run selected tests");
		
		ToolItem tltmRunselectedtest = new ToolItem(toolBar, SWT.NONE);
		tltmRunselectedtest.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/10.png"));
		tltmRunselectedtest.setToolTipText("Run current test");
		
		ToolItem tltmStop = new ToolItem(toolBar, SWT.NONE);
		tltmStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTestTreeState();
			}
		});
		tltmStop.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/actions/8.png"));
		tltmStop.setToolTipText("Stop");

		Label toolBarSeparator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_toolBarSeparator = new FormData();
		fd_toolBarSeparator.top = new FormAttachment(toolBar);
		fd_toolBarSeparator.right = new FormAttachment(100);
		fd_toolBarSeparator.left = new FormAttachment(0);
		toolBarSeparator.setLayoutData(fd_toolBarSeparator);

		sashForm = new SashForm(shell, SWT.VERTICAL);
		FormData fd_sashForm = new FormData();
		fd_sashForm.top = new FormAttachment(toolBarSeparator, 4);
		fd_sashForm.right = new FormAttachment(100);
		fd_sashForm.bottom = new FormAttachment(100);
		fd_sashForm.left = new FormAttachment(0);
		sashForm.setLayoutData(fd_sashForm);
		
		Composite compositeTree = new Composite(sashForm, SWT.NONE);
		compositeTree.setLayout(new FormLayout());
		
		Label lblTestHierarchy = new Label(compositeTree, SWT.NONE);
		FormData fd_lblTestHierarchy = new FormData();
		fd_lblTestHierarchy.right = new FormAttachment(100);
		fd_lblTestHierarchy.top = new FormAttachment(0);
		fd_lblTestHierarchy.left = new FormAttachment(0);
		lblTestHierarchy.setLayoutData(fd_lblTestHierarchy);
		lblTestHierarchy.setText("Test Hi&erarchy:");
		
		testTree = new Tree(compositeTree, SWT.BORDER | SWT.CHECK);
		testTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.item != null && testTree.getSelectionCount() > 0 && e.item == testTree.getSelection()[0]) {
					tableFailureList.deselectAll();
					for (TableItem tableItem: tableFailureList.getItems()) {
						if (tableItem.getData() == e.item) {
							tableFailureList.setSelection(tableItem);
							break;
						}
					}
				}
				if (e.detail == SWT.CHECK) {
					updateStatus(true);
				}
			}
		});
		testTree.setToolTipText("Hierarchy of test cases. Checked test cases will be run.");
		FormData fd_testTree = new FormData();
		fd_testTree.top = new FormAttachment(lblTestHierarchy, 4);
		fd_testTree.left = new FormAttachment(0);
		fd_testTree.bottom = new FormAttachment(100, -3);
		fd_testTree.right = new FormAttachment(100);
		testTree.setLayoutData(fd_testTree);
		
		TreeItem trtmTest = new TreeItem(testTree, SWT.NONE);
		trtmTest.setChecked(true);
		trtmTest.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png"));
		trtmTest.setText("Test1");
		
		TreeItem trtmSubtest = new TreeItem(trtmTest, SWT.NONE);
		trtmSubtest.setChecked(true);
		trtmSubtest.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png"));
		trtmSubtest.setText("SubTest");
		
		TreeItem trtmSubtest_1 = new TreeItem(trtmTest, SWT.NONE);
		trtmSubtest_1.setChecked(true);
		trtmSubtest_1.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png"));
		trtmSubtest_1.setText("SubTest1.2");
		trtmTest.setExpanded(true);
		
		TreeItem trtmTest_1 = new TreeItem(testTree, SWT.NONE);
		trtmTest_1.setChecked(true);
		trtmTest_1.setImage(SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png"));
		trtmTest_1.setText("Test2");
		
		compositeResults = new Composite(sashForm, SWT.NONE);
		compositeResults.setLayout(new FormLayout());
		
		Composite compositeProgressAndScore = new Composite(compositeResults, SWT.BORDER);
		compositeProgressAndScore.setToolTipText("");
		compositeProgressAndScore.setLayout(new FormLayout());
		FormData fd_compositeProgressAndScore = new FormData();
		fd_compositeProgressAndScore.bottom = new FormAttachment(0, 52);
		fd_compositeProgressAndScore.right = new FormAttachment(100);
		fd_compositeProgressAndScore.top = new FormAttachment(0, 3);
		fd_compositeProgressAndScore.left = new FormAttachment(0);
		compositeProgressAndScore.setLayoutData(fd_compositeProgressAndScore);
		
		Label lblProgress = new Label(compositeProgressAndScore, SWT.RIGHT);
		FormData fd_lblProgress = new FormData();
		fd_lblProgress.right = new FormAttachment(0, 59);
		fd_lblProgress.top = new FormAttachment(0, 6);
		fd_lblProgress.left = new FormAttachment(0, 10);
		lblProgress.setLayoutData(fd_lblProgress);
		lblProgress.setText("Progress:");
		
		progressBar = new ProgressBar(compositeProgressAndScore, SWT.NONE);
		progressBar.setToolTipText("Shows the proportion of tests run");
		FormData fd_progressBar = new FormData();
		fd_progressBar.height = 12;
		fd_progressBar.top = new FormAttachment(0, 6);
		fd_progressBar.left = new FormAttachment(lblProgress, 6);
		fd_progressBar.right = new FormAttachment(100, -6);
		progressBar.setLayoutData(fd_progressBar);
		
		Label lblScore = new Label(compositeProgressAndScore, SWT.RIGHT);
		FormData fd_lblScore = new FormData();
		fd_lblScore.right = new FormAttachment(lblProgress, 0, SWT.RIGHT);
		fd_lblScore.top = new FormAttachment(lblProgress, 6);
		fd_lblScore.left = new FormAttachment(lblProgress, 0, SWT.LEFT);
		lblScore.setLayoutData(fd_lblScore);
		lblScore.setText("Score:");
		
		scoreBar = new ProgressBar(compositeProgressAndScore, SWT.BORDER);
		scoreBar.setToolTipText("Shows the proportion of successful tests");
		FormData fd_scoreBar = new FormData();
		fd_scoreBar.right = new FormAttachment(100, -56);
		fd_scoreBar.height = 12;
		fd_scoreBar.top = new FormAttachment(progressBar, 6);
		fd_scoreBar.left = new FormAttachment(progressBar, 0, SWT.LEFT);
		scoreBar.setLayoutData(fd_scoreBar);
		
		lblProgressPercent = new Label(compositeProgressAndScore, SWT.RIGHT);
		FormData fd_lblProgressPercent = new FormData();
		fd_lblProgressPercent.top = new FormAttachment(lblScore, 0, SWT.TOP);
		fd_lblProgressPercent.left = new FormAttachment(scoreBar, 6);
		fd_lblProgressPercent.right = new FormAttachment(100, -6);
		lblProgressPercent.setLayoutData(fd_lblProgressPercent);
		lblProgressPercent.setText("Progress");
		
		tableResults = new Table(compositeResults, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		tableResults.setToolTipText("Shows statistics about the current/last run");
		FormData fd_tableResults = new FormData();
		fd_tableResults.height = 16;
		fd_tableResults.top = new FormAttachment(compositeProgressAndScore, 3);
		fd_tableResults.right = new FormAttachment(100, 0);
		fd_tableResults.left = new FormAttachment(0, 0);
		tableResults.setLayoutData(fd_tableResults);
		tableResults.setHeaderVisible(true);
		
		TableColumn tableColumn = new TableColumn(tableResults, SWT.NONE);
		tableColumn.setWidth(8);
		
		TableColumn tblclmnTests = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnTests.setWidth(70);
		tblclmnTests.setText("Tests");
		
		TableColumn tblclmnRun = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnRun.setWidth(70);
		tblclmnRun.setText("Run");
		
		TableColumn tblclmnFailures = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnFailures.setWidth(70);
		tblclmnFailures.setText("Failures");
		
		TableColumn tblclmnErrors = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnErrors.setWidth(70);
		tblclmnErrors.setText("Errors");
		
		TableColumn tblclmnOverrides = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnOverrides.setWidth(70);
		tblclmnOverrides.setText("Overrides");
		
		TableColumn tblclmnTestTime = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnTestTime.setWidth(52);
		tblclmnTestTime.setText("Test Time");
		
		TableColumn tblclmnTotalTime = new TableColumn(tableResults, SWT.RIGHT);
		tblclmnTotalTime.setWidth(70);
		tblclmnTotalTime.setText("Total Time");
		
		TableItem tableItem = new TableItem(tableResults, SWT.NONE);
		tableItem.setText(new String[] {"", "1", "2", "3", "4", "5", "6", "7"});

		tableFailureList = new Table(compositeResults, SWT.BORDER | SWT.FULL_SELECTION);
		tableFailureList.setToolTipText("Shows the list of failed tests");
		FormData fd_tableFailureList = new FormData();
		fd_tableFailureList.bottom = new FormAttachment(100, -3);
		fd_tableFailureList.left = new FormAttachment(0, 0);
		fd_tableFailureList.right = new FormAttachment(100, 0);
		fd_tableFailureList.top = new FormAttachment(tableResults, 3);
		tableFailureList.setLayoutData(fd_tableFailureList);
		tableFailureList.setHeaderVisible(true);
		
		TableColumn tblclmnTestName = new TableColumn(tableFailureList, SWT.NONE);
		tblclmnTestName.setWidth(120);
		tblclmnTestName.setText("Test Name");
		
		TableColumn tblclmnFailureType = new TableColumn(tableFailureList, SWT.NONE);
		tblclmnFailureType.setWidth(100);
		tblclmnFailureType.setText("Failure Type");
		
		TableColumn tblclmnMessage = new TableColumn(tableFailureList, SWT.NONE);
		tblclmnMessage.setWidth(200);
		tblclmnMessage.setText("Message");
		
		TableColumn tblclmnLocation = new TableColumn(tableFailureList, SWT.NONE);
		tblclmnLocation.setWidth(60);
		tblclmnLocation.setText("Location");
		
		compositeErrorBox = new Composite(sashForm, SWT.NONE);
		compositeErrorBox.setLayout(new FormLayout());
		
		errorMessageStyledText = new StyledText(compositeErrorBox, SWT.BORDER);
		errorMessageStyledText.setText("ErrorMessageRTF");
		FormData fd_errorMessageStyledText = new FormData();
		fd_errorMessageStyledText.bottom = new FormAttachment(100, -3);
		fd_errorMessageStyledText.right = new FormAttachment(100);
		fd_errorMessageStyledText.top = new FormAttachment(0, 3);
		fd_errorMessageStyledText.left = new FormAttachment(0);
		errorMessageStyledText.setLayoutData(fd_errorMessageStyledText);
		sashForm.setWeights(new int[] {200, 150, 50});

	}

	void hideTestNodesActionExecute() {
		if (testTree.getItemCount() == 0)
			return;

		testTree.setRedraw(false);
		try {
			if (testTree.getItemCount() > 0) { // XXX only one root node is processed
				TreeItem node = testTree.getItem(0);
				node.setExpanded(true);
				collapseNonGrandparentNodes(node);
				selectNode(node);
			}
		} finally {
			testTree.setRedraw(true);
			testTree.update();
		}
	}

	void expandAllNodesActionExecute() {
		for(TreeItem node: getAllTestTreeItems())
			node.setExpanded(true);
		if (testTree.getSelectionCount() > 0)
			testTree.showSelection();
		else if (testTree.getItemCount() > 0)
			testTree.select(testTree.getItem(0)); // XXX may be need call testTree.showSelection();
	}

	private void runActionExecute() {
		if (fSuite == null)
			return;

		setup();
		runTheTest();
	}


	private void resetProgress() {
		scoreBar.setBackground(null);
		scoreBar.update();
		scoreBar.setSelection(0);
		progressBar.setSelection(0);
		lblProgressPercent.setText("");
		
	}

	private int fPopupX;
	private int fPopupY;

	protected Class<?> fSuite;
	protected Result fTestResult;
	protected boolean fRunning;
	protected ArrayList<Description> fTests;
	private Map<Description, TreeItem> testToNodeMap;
	protected List<Description> fSelectedTests;
	protected long fTotalTime;
	protected int fFailureCount;
	protected int fTotalTestCount;

	protected void setup() {
		tableFailureList.clearAll();
		resetProgress();
		shell.update();

		TableItem item = tableResults.getItem(0);
		if (fSuite != null) {
			int i = countEnabledTestCases();
			item.setText(1, Integer.toString(i));
			progressBar.setMaximum(i);
		} else {
			item.setText(1, "");
			progressBar.setMaximum(10000);
		}
		scoreBar.setMaximum(progressBar.getMaximum());
		
		item.setText(2, "");
		item.setText(3, "");
		item.setText(4, "");
		item.setText(5, "");
		item.setText(6, "");
		item.setText(7, "");

		Deque<TreeItem> stack = new ArrayDeque<>();
		stack.addAll(Arrays.asList(testTree.getItems()));
		while (stack.size() > 0) {
			TreeItem node = stack.pop();
			node.setImage(imgNone);
			stack.addAll(Arrays.asList(node.getItems()));
		}
		updateTestTreeState();
	}

	protected void setUpStateImages() {
		imgNone = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/0.png");
		imgRunning = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/1.png");
		imgRun = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/2.png");
		imgHasProps = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/3.png");
		imgFailed = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/4.png");
		imgError = SWTResourceManager.getImage(TestRunner.class, "/ru/vitkud/test/images/run/5.png");
		indexesImages = new HashMap<>();
		indexesImages.put(imgNone, 0);
		indexesImages.put(imgRunning, 1);
		indexesImages.put(imgRun, 2);
		indexesImages.put(imgHasProps, 3);
		indexesImages.put(imgFailed, 4);
		indexesImages.put(imgError, 5);
		initColors();
	}

	private void initColors() {
		clOk = display.getSystemColor(SWT.COLOR_GREEN);
		clFailure = display.getSystemColor(SWT.COLOR_MAGENTA);
		clError = display.getSystemColor(SWT.COLOR_RED);
	}

	public void setSuite(Class<?> value) {
		fSuite = value;
		if (fSuite != null) {
			//loadSuiteConfiguration(); // moved into initTree()
			enableUI(true);
			initTree();
		} else {
			enableUI(false);
		}
	}

	protected void clearFailureMessage() {
		errorMessageStyledText.setText("");
	}


	protected TableItem addFailureItem(Failure failure) {
		// TODO Auto-generated method stub
		return null;
	}

	private String formatElapsedTime(long milli) {
		long h = milli / 3600000;
		milli = milli % 3600000;
		long nn = milli / 60000;
		milli = milli % 60000;
		long ss = milli / 1000;
		milli = milli % 1000;
		long zzz = milli;
		return String.format("%d:%02d:%02d.%03d", h, nn, ss, zzz);
	}

	protected void updateStatus(boolean fullUpdate) {
		if (tableResults.getItemCount() == 0)
			return;

		if (fullUpdate) {
			fTotalTestCount = countEnabledTestCases();
			if (fSuite != null)
				tableResults.getItem(0).setText(1, Integer.toString(fTotalTestCount));
			else
				tableResults.getItem(0).setText(1, "");
		}

		if (fTestResult != null) {
			// Save the test number as we use it a lot
			int testNumber = fTestResult.getRunCount();

			if (fullUpdate || (testNumber & 15) == 0) {
				TableItem item = tableResults.getItem(0);
				item.setText(2, Integer.toString(testNumber));
				item.setText(3, Integer.toString(fTestResult.getFailureCount()));
				item.setText(4, Integer.toString(fTestResult.getIgnoreCount())); // XXX (errorCount)
				item.setText(5, Integer.toString(fTestResult.getIgnoreCount())); // XXX (Overrides)
				item.setText(6, formatElapsedTime(fTestResult.getRunTime()));
				item.setText(7, formatElapsedTime(Math.max(fTestResult.getRunTime(), fTotalTime)));

				scoreBar.setSelection(testNumber - (fTestResult.getFailureCount() + fTestResult.getIgnoreCount()));
				progressBar.setSelection(testNumber);

				// There is a possibility for zero tests
				if (testNumber == 0 && fTotalTestCount == 0)
					lblProgressPercent.setText("100%");
				else
					lblProgressPercent.setText(Integer.toString(100 * scoreBar.getSelection() / scoreBar.getMaximum()) + "%");
			}
			// Allow just the results pane to catch up

			compositeResults.update();
	    } else {
	    	TableItem item = tableResults.getItem(0);
	    	if (item.getText(1).equals("0") || item.getText(1).equals("")) {
	    		for (int i = 2; i <= 7; ++i)
	    			item.setText(i, "");
	    	} else if (!item.getText(1).equals(item.getText(2))) {
	    		for (int i = 2; i <= 7; ++i)
	    			item.setText(i, "");
	    	} else {
				item.setText(6, formatElapsedTime(getElapsedTestTime(getSelectedTest())));
				item.setText(7, formatElapsedTime(Math.max(getElapsedTestTime(getSelectedTest()), fTotalTime)));
	    	}

	    	resetProgress();
	    }

		if (fullUpdate) {
			// Allow the whole display to catch up and check for key strokes

			shell.update();
			processMessages();
		}
	}


	protected void fillTestTree() {
		testTree.removeAll();
		fTests.clear();
		testToNodeMap.clear();

		Request request = Request.aClass(fSuite);
		Runner runner = request.getRunner();
		Description rootDescription = runner.getDescription();

		TreeItem rootItem = new TreeItem(testTree, SWT.NONE);

		fillTestTree(rootItem, rootDescription);
	}

	protected void fillTestTree(TreeItem treeItem, Description description) {
		if (description == null)
			return;

		treeItem.setText(getShortDisplayName(description));
		treeItem.setData(fTests.size());

		fTests.add(description);
		testToNodeMap.put(description, treeItem);

		for (Description childDescripton: description.getChildren()) {
			fillTestTree(new TreeItem(treeItem, SWT.NONE), childDescripton);
		}
	}


	protected void updateNodeImage(TreeItem node) {
		if (node.getChecked() && node.getParentItem() != null && 
				(!node.getParentItem().getChecked() || node.getParentItem().getGrayed())) {
			node.setGrayed(true);
		} else { 
			node.setGrayed(false);
		}
	}

	protected void updateNodeState(TreeItem node) {
		assert node != null;
		Description test = nodeToTest(node);
		assert test != null;

		updateNodeImage(node);

		for (TreeItem childNode: node.getItems()) {
			updateNodeState(childNode);
		}
	}

	protected void updateTestTreeState() {
		if (testTree.getItemCount() > 0) {
			testTree.setRedraw(false);
			try {
				for (TreeItem node: testTree.getItems()) {
					updateNodeState(node);
				}
			} finally {
				testTree.setRedraw(true);
				testTree.update();
			}
		}
	}


	protected void makeNodeVisible(TreeItem node) {
		node.getParent().showItem(node);
	}

	protected void setTreeNodeImage(TreeItem node, Image image) {
		while (node != null) {
			if (indexesImages.get(image).intValue() > indexesImages.get(node.getImage()).intValue()) {
				node.setImage(image);
			}
			if (image == imgRunning)
				node = null;
			else 
				node = node.getParentItem();
		}
	}

	protected void selectNode(TreeItem node) {
		node.getParent().select(node);
		makeNodeVisible(node);
	}


	protected Description nodeToTest(TreeItem node) {
		assert node != null;

		int idx = (int) node.getData();
		assert idx >=0 && idx < fTests.size();
		return fTests.get(idx);
	}

	protected TreeItem testToNode(Description test) {
		return testToNodeMap.get(test);
	}


	protected void enableUI(boolean enable) {
		mntmSelectAll.setEnabled(enable);
		tltmSelectAll.setEnabled(enable);
		mntmDeselectAll.setEnabled(enable);
		tltmDeselectAll.setEnabled(enable);
		mntmSelectFailed.setEnabled(enable);
		tltmSelectFailed.setEnabled(enable);
		mntmSelectCurrent.setEnabled(enable);
		tltmSelectCurrent.setEnabled(enable);
		mntmDeselectCurrent.setEnabled(enable);
		tltmDeselectCurrent.setEnabled(enable);
		mntmHideTestNodes.setEnabled(enable);
		mntmExpandAll.setEnabled(enable);
	}

	protected void runTheTest() {
		if (fSuite == null)
			return;

		if (fRunning) {
			// warning: we're reentering this method if fRunning is true
			// TODO assert(fTestResult != null);
			// TODO fTestResult.stop();
			return;
		}

		fRunning = true;
		try {

			Request request = Request.aClass(fSuite).filterWith(new RunTheTestFilter());
			Runner runner = request.getRunner();

			// TODO RunAction.Enabled  := False;
			// TODO StopAction.Enabled := True;

			// TODO CopyMessageToClipboardAction.Enabled := false;

			enableUI(false);
			// TODO autoSaveConfiguration();
			// TODO clearResult();

			final RunNotifier notifier = new RunNotifier();
			notifier.addListener(new RunTheTestListener());
			fTestResult = new Result();
			RunListener listener = fTestResult.createListener();
			notifier.addListener(listener);
			try {
				// TODO TestResult.BreakOnFailures := BreakOnFailuresAction.Checked;
				// TODO TestResult.FailsIfNoChecksExecuted := FailIfNoChecksExecutedAction.Checked;
				// TODO TestResult.FailsIfMemoryLeaked := FailTestCaseIfMemoryLeakedAction.Checked;
				// TODO TestResult.IgnoresMemoryLeakInSetUpTearDown := IgnoreMemoryLeakInSetUpTearDownAction.Checked;
				notifier.fireTestRunStarted(runner.getDescription());
				runner.run(notifier);
				notifier.fireTestRunFinished(fTestResult);
			} catch (StoppedByUserException e) {
				// not interesting
			} finally {
				// TODO ? FErrorCount := TestResult.ErrorCount;
				//fIgnoreCount = result.getIgnoreCount();
				fFailureCount = fTestResult.getFailureCount();
				// TODO ? TestResult.Free;
				// TODO ?TestResult := nil;
			}
		} finally {
			fRunning = false;
			enableUI(true);
		}
	}


	protected void initTree() {
		//fTests.clear();
		fillTestTree(/*suite*/);
		loadSuiteConfiguration(); // moved from setSuite()
		setup();
		if (mntmHideTestNodesOnOpen.getSelection()) {
			hideTestNodesActionExecute();
		} else {
			expandAllNodesActionExecute();
		}
		if (testTree.getItemCount() > 0)
			testTree.setSelection(testTree.getItem(0));
	}

	protected String iniFileName() {
		return new File(System.getProperty("user.dir"), TEST_INI_FILE).getPath();
	}

	protected ICustomIniFile getIniFile(String fileName) {
		if (mntmUseRegistry.getSelection()) {
			// TODO: tRegistryIniFile.Create( GetDUnitRegistryKey + FileName )
			return null;
		} else
			return new TIniFile(fileName);
	}

	protected void writeToIniFile(String fileName, Properties props) {
		if (mntmUseRegistry.getSelection()) {
			// TODO: ...
		} else {
			try (OutputStream os = new FileOutputStream(fileName)) {
				props.store(os, null);
			} catch (IOException e) {
				// XXX do nothing
				e.printStackTrace();
			}
		}
	}


	private void loadRegistryAction() {
		try (ICustomIniFile ini = new TIniFile(iniFileName())) {
			mntmUseRegistry.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"UseRegistry", mntmUseRegistry.getSelection()));
		}
	}

	private void saveRegistryAction() {
		if (mntmUseRegistry.getSelection())
			new File(iniFileName()).delete();

		try (ICustomIniFile ini = new TIniFile(iniFileName())) {
			ini.writeBool(CN_CONFIG_INI_SECTION, "UseRegistry", mntmUseRegistry.getSelection());
		}
	}


	private void loadFormPlacement() {
		try (ICustomIniFile ini = getIniFile(iniFileName())) {
			shell.setBounds(
					ini.readInteger(CN_CONFIG_INI_SECTION, "Left", shell.getLocation().x),
					ini.readInteger(CN_CONFIG_INI_SECTION, "Top", shell.getLocation().y),
					ini.readInteger(CN_CONFIG_INI_SECTION, "Width", shell.getSize().x),
					ini.readInteger(CN_CONFIG_INI_SECTION, "Height", shell.getSize().y)
			);
			if (ini.readBool(CN_CONFIG_INI_SECTION, "Maximized", false))
				shell.setMaximized(true);
		}
	}

	private void saveFormPlacement() {
		try (ICustomIniFile ini = getIniFile(iniFileName())) {
			ini.writeBool(CN_CONFIG_INI_SECTION, "AutoSave", mntmAutoSave.getSelection());

			if (!shell.getMaximized()) {
				ini.writeInteger(CN_CONFIG_INI_SECTION, "Left", shell.getLocation().x);
				ini.writeInteger(CN_CONFIG_INI_SECTION, "Top", shell.getLocation().y);
				ini.writeInteger(CN_CONFIG_INI_SECTION, "Width", shell.getSize().x);
				ini.writeInteger(CN_CONFIG_INI_SECTION, "Height", shell.getSize().y);
			}

			ini.writeBool(CN_CONFIG_INI_SECTION, "Maximized", shell.getMaximized());
		}
		
	}


	protected void saveConfiguration() {
		if (fSuite != null)
			saveSuiteConfiguration();

		saveFormPlacement();
		saveRegistryAction();

		try (ICustomIniFile ini = getIniFile(iniFileName())) {
				// splitter locations
			int[] weights = sashForm.getWeights();
			for (int i = 0; i < weights.length; ++i) {
				ini.writeInteger(CN_CONFIG_INI_SECTION, "sashForm.weights[" + i + "]", weights[i]);
			}

			// error box
			ini.writeBool(CN_CONFIG_INI_SECTION, "ErrorMessage.Visible", mntmErrorBoxVisible.getSelection());

			// failure list configuration
			for (int i = 0; i < tableFailureList.getColumnCount(); ++i) {
				ini.writeInteger(CN_CONFIG_INI_SECTION, "FailureList.ColumnWidth[" + i + "]",
						tableFailureList.getColumn(i).getWidth());
			}

		    // other options
			ini.writeBool(CN_CONFIG_INI_SECTION, "HideTestNodesOnOpen", mntmHideTestNodesOnOpen.getSelection());
			ini.writeBool(CN_CONFIG_INI_SECTION, "BreakOnFailures", mntmBreakOnFailures.getSelection());
			ini.writeBool(CN_CONFIG_INI_SECTION, "FailOnNoChecksExecuted", mntmFailTestCaseIfNoChecksExecuted.getSelection());
			ini.writeBool(CN_CONFIG_INI_SECTION, "FailOnMemoryLeaked", mntmFailTestCaseIfMemoryLeaked.getSelection());
			ini.writeBool(CN_CONFIG_INI_SECTION, "IgnoreSetUpTearDownLeaks", mntmIgnoreMemoryLeakInSetUpTearDown.getSelection());
			ini.writeBool(CN_CONFIG_INI_SECTION, "ReportMemoryLeakTypes", mntmReportMemoryLeakTypeOnShutdown.getSelection());
			ini.writeBool(CN_CONFIG_INI_SECTION, "SelectTestedNode", mntmShowTestedNode.getSelection()); 
			ini.writeBool(CN_CONFIG_INI_SECTION, "WarnOnFailTestOverride", mntmWarnIfFailTestOverridden.getSelection());
			ini.writeInteger(CN_CONFIG_INI_SECTION, "PopupX", fPopupX);
			ini.writeInteger(CN_CONFIG_INI_SECTION, "PopupY", fPopupY);
		}
	}

	protected void loadConfiguration() {
		loadRegistryAction();
		loadFormPlacement();
		loadSuiteConfiguration();

		try (ICustomIniFile ini = getIniFile(iniFileName())) {
			mntmAutoSave.setSelection(ini.readBool(CN_CONFIG_INI_SECTION, "AutoSave", mntmAutoSave.getSelection()));

			// splitter locations
			int[] weights = sashForm.getWeights();
			for (int i = 0; i < weights.length; ++i) {
				weights[i] = ini.readInteger(CN_CONFIG_INI_SECTION, "sashForm.weights[" + i + "]", weights[i]);
			}
			sashForm.setWeights(weights);
	
			// error box
			mntmErrorBoxVisible.setSelection(ini.readBool(CN_CONFIG_INI_SECTION, "ErrorMessage.Visible",
					mntmErrorBoxVisible.getSelection()));
	
			compositeErrorBox.setVisible(mntmErrorBoxVisible.getSelection());
			// XXX fix SWT problem
			if (!mntmErrorBoxVisible.getSelection()) {
				Point size = shell.getSize();
				shell.setSize(size.x + 1, size.y);
				shell.setSize(size);
			}

			// failure list configuration
			for (int i = 0; i < tableFailureList.getColumnCount(); ++i) {
				TableColumn column = tableFailureList.getColumn(i);
				column.setWidth(ini.readInteger(CN_CONFIG_INI_SECTION, "FailureList.ColumnWidth[" + i + "]", column.getWidth()));
			}
	
		    // other options
			mntmHideTestNodesOnOpen.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"HideTestNodesOnOpen", mntmHideTestNodesOnOpen.getSelection()));
			mntmBreakOnFailures.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"BreakOnFailures", mntmBreakOnFailures.getSelection()));
			mntmFailTestCaseIfNoChecksExecuted.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"FailOnNoChecksExecuted", mntmFailTestCaseIfNoChecksExecuted.getSelection()));
			mntmFailTestCaseIfMemoryLeaked.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"FailOnMemoryLeaked", mntmFailTestCaseIfMemoryLeaked.getSelection()));
			mntmIgnoreMemoryLeakInSetUpTearDown.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"IgnoreSetUpTearDownLeaks", mntmIgnoreMemoryLeakInSetUpTearDown.getSelection()));
			mntmReportMemoryLeakTypeOnShutdown.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"ReportMemoryLeakTypes", mntmReportMemoryLeakTypeOnShutdown.getSelection()));
			mntmWarnIfFailTestOverridden.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"WarnOnFailTestOverride", mntmWarnIfFailTestOverridden.getSelection()));
			mntmShowTestedNode.setSelection(ini.readBool(CN_CONFIG_INI_SECTION,
					"SelectTestedNode", mntmShowTestedNode.getSelection()));
			fPopupX = ini.readInteger(CN_CONFIG_INI_SECTION, "PopupX", 350);
			fPopupY = ini.readInteger(CN_CONFIG_INI_SECTION, "PopupY", 30);
		}

		if (fSuite != null)
			updateTestTreeState();
	}

	private void saveSuiteConfiguration(ICustomIniFile ini, final String section, final TreeItem node) {
		String testShortDisplayName = getShortDisplayName(nodeToTest(node));
		if (node.getChecked())
			ini.deleteKey(section, testShortDisplayName);
		else
			ini.writeBool(section, testShortDisplayName, false);

		for(TreeItem childNode: node.getItems()) {
			saveSuiteConfiguration(ini, section + "." + testShortDisplayName, childNode);
		}
	}

	protected void saveSuiteConfiguration() {
		if (fSuite == null)
			return;

		try (ICustomIniFile ini = getIniFile(iniFileName())) {
			for (TreeItem node: testTree.getItems())
				saveSuiteConfiguration(ini, "Tests", node);
		}
	}

	private void loadSuiteConfiguration(ICustomIniFile ini, final String section, final TreeItem node) {
		Description test = nodeToTest(node);
		node.setChecked(ini.readBool(section, getShortDisplayName(test), true));

		for(TreeItem childNode: node.getItems()) {
			loadSuiteConfiguration(ini, section + "." + getShortDisplayName(test), childNode);
		}
	}
	
	protected void loadSuiteConfiguration() {
		if (fSuite == null)
			return;

		try (ICustomIniFile ini = getIniFile(iniFileName())) {
			for (TreeItem node: testTree.getItems())
				loadSuiteConfiguration(ini, "Tests", node);
		}
	}

	protected void autoSaveConfiguration() {
		if (mntmAutoSave.getSelection())
			saveConfiguration();
	}


	protected boolean nodeIsGrandparent(TreeItem node) {
		boolean result = false;
		for (TreeItem childNode: node.getItems()) {
			result = childNode.getItemCount() > 0 || result;
			collapseNonGrandparentNodes(childNode);
		}
		return result;
	}

	protected void collapseNonGrandparentNodes(TreeItem rootNode) {
		if (!nodeIsGrandparent(rootNode))
			rootNode.setExpanded(false);

		for (TreeItem childNode: rootNode.getItems()) {
			collapseNonGrandparentNodes(childNode);
		}
	}


	protected void clearStatusMessage() {
		errorMessageStyledText.setText("");
	}


	protected void setupCustomShortcuts() {
		// TODO Auto-generated method stub
		
	}

	protected void setupGuiNodes() {
		// do nothing: testToNodeMap already filled in fillTestTree()
	}

	private class RunTheTestFilter extends Filter {

		@Override
		public boolean shouldRun(Description description) {
			if (fSelectedTests == null) {
				TreeItem node = testToNode(description);
				return node != null && node.getChecked();
			} else {
				return fSelectedTests.contains(description);
			}
		}

		@Override
		public String describe() {
			return getClass().getSimpleName();
		}

	}


	private class RunTheTestListener extends RunListener {

		boolean lastTestFail;

		// This may be called on an arbitrary thread.
		@Override
		public void testRunStarted(final Description description) throws Exception {
	    	if (display.isDisposed()) return;
	    	display.syncExec(new Runnable() {
				@Override public void run() {
			    	if (display.isDisposed()) return;
					testingStarts(description);
				}
			});
		}

		// This may be called on an arbitrary thread.
		@Override
		public void testRunFinished(final Result result) throws Exception {
	    	if (!display.isDisposed()) {
	    		display.syncExec(new Runnable() {
	    			@Override public void run() {
	    				if (!display.isDisposed())
	    					testingEnds(result);
	    			}
	    		});
	    	}
		}

		public void testStarted(final Description description) throws Exception {
			lastTestFail = false;
	    	if (!display.isDisposed()) {
	    		display.syncExec(new Runnable() {
	    			@Override public void run() {
	    				if (!display.isDisposed())
	    					startTest(description);
	    			}
	    		});
	    	}
		}

		public void testFinished(final Description description) throws Exception {
	    	if (!display.isDisposed()) {
	    		display.syncExec(new Runnable() {
	    			@Override public void run() {
	    				if (!display.isDisposed()) {
	    					if (!lastTestFail)
	    						addSuccess(description);
	    					endTest(description);
	    				}
	    			}
	    		});
	    	}
		}

		//  may be called on an arbitrary thread.
		public void testFailure(final Failure failure) throws Exception {
			lastTestFail = true;
			if (!display.isDisposed()) {
				display.syncExec(new Runnable() {
					@Override public void run() {
						if (!display.isDisposed()) {
							if (failure.getException() instanceof AssertionError)
								addFailure(failure);
							else 
								addError(failure);
						}
					}
				});
			}
		}

		public void testAssumptionFailure(final Failure failure) {
			lastTestFail = true;
			if (!display.isDisposed()) {
				display.syncExec(new Runnable() {
					@Override public void run() {
						if (!display.isDisposed()) {
							addFailure(failure); // TODO ...
						}
					}
				});
			}
		}

		public void testIgnored(Description description) throws Exception {
			// TODO ..
		}

	}


	public void testingStarts(Description description) {
    	fTotalTime = 0;
    	updateStatus(true);
    	scoreBar.setBackground(clOk);
    	scoreBar.update();
	}

	public void testingEnds(Result result) {
		fTotalTime = result.getRunTime();
	}

	public void startTest(Description description) {
    	assert fTestResult != null;
    	assert description != null;
    	TreeItem node = testToNode(description);
    	assert node != null;
    	setTreeNodeImage(node, imgRunning);
		if (mntmShowTestedNode.getSelection()) {
			makeNodeVisible(node);
			testTree.update();
		}
		clearStatusMessage();
		updateStatus(false);
	}

	public void endTest(Description description) {
		updateStatus(false);
	}

	public void addSuccess(Description description) {
		assert description != null;
		setTreeNodeImage(testToNode(description), imgRun);
	}

	public void addError(Failure failure) {
		TableItem item = addFailureItem(failure);
		item.setImage(imgError);
		scoreBar.setBackground(clError);
		scoreBar.update();

		setTreeNodeImage(testToNode(failure.getDescription()), imgError);
		updateStatus(false);
	}

	public void addFailure(Failure failure) {
		TableItem item = addFailureItem(failure);
		item.setImage(imgFailed);
		if (!clError.equals(scoreBar.getBackground())) {
			scoreBar.setBackground(clFailure);
			scoreBar.update();
		}
		setTreeNodeImage(testToNode(failure.getDescription()), imgFailed);
		updateStatus(false);
	}


	private long getElapsedTestTime(Description test) {
		// TODO Auto-generated method stub
		return 0;
	}

	private Description getSelectedTest() {
		if (testTree.getSelectionCount() == 0)
			return null;
		return fTests.get((int) testTree.getSelection()[0].getData());
	}

	private int countEnabledTestCases(TreeItem treeItem) {
		int result = 0;
		if (treeItem.getChecked()) {
			for (TreeItem childItem: treeItem.getItems()) {
				result += countEnabledTestCases(childItem);
			}
			if ((fTests.get((int) treeItem.getData())).isTest())
				result++;
		}
		return result;
	}
	
	private int countEnabledTestCases() {
		int result = 0;
		for (TreeItem treeItem: testTree.getItems()) {
			result += countEnabledTestCases(treeItem);
		}
		return result;
	}

	public int getfFailureCount() {
		return fFailureCount;
	}

	private String getShortDisplayName(Description test) {
		String result = test.getDisplayName();
		if (test.isTest()) {
			String suffix = "(" + test.getClassName() + ")";
			if (test.getDisplayName().endsWith(suffix))
				result = result.substring(0, result.length() - suffix.length());
		}
		if (test.isSuite()) {
			int pos = Math.max(result.lastIndexOf('.'), result.lastIndexOf('$'));
			if (pos >= 0)
				result = result.substring(pos + 1);
		}
		return result;
	}

}
