package edu.agh.klaukold.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.agh.R;
import edu.agh.klaukold.common.Box;
import edu.agh.klaukold.common.Line;
import edu.agh.klaukold.common.Point;
import edu.agh.klaukold.common.Root;
import edu.agh.klaukold.common.Text;
import edu.agh.klaukold.enums.Align;
import edu.agh.klaukold.enums.BlockShape;
import edu.agh.klaukold.enums.LineThickness;
import edu.agh.klaukold.utilities.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RotateDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

public class DrawView extends RelativeLayout {
    private Paint paint = new Paint();
    public List<Line> lines;
    public float transx, transy, zoomx = 1, zoomy = 1;
    private int[] position = new int[2];
    private static int activeColor;
    private static int collapsedColor;
    private Canvas canvas;
    // private LinkedList<Style> style = new LinkedList<Style>();

    public DrawView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        getLocationInWindow(position);
        setWillNotDraw(false);
        lines = new ArrayList<Line>();
    }

    private List<Box> detMoveLeftUp = new ArrayList<Box>();
    private List<Box> detMoveLeftDown = new ArrayList<Box>();
    private List<Box> detMoveRightUp = new ArrayList<Box>();
    private List<Box> detMoveRightDown = new ArrayList<Box>();

    public boolean updateLeft;
    public boolean updateRight;
    private Context context;

    private boolean revalidate = true;

    public static void setActiveColor(String rgb) {
        activeColor = Color.parseColor(rgb);
    }

    public static void setCollapsedColor(String rgb) {
        collapsedColor = Color.parseColor(rgb);
    }

    public void revalidate() {
        revalidate = true;

    }

//   //próby wymuszenia odstępów na bloczkach swobodnych
//   private void triggerDetachMove(Box box) {
//	   Core c = MainActivity.core;
//	   if(box.rect.left <= c.rect.left) {
//		   if(!c.left.isEmpty()) {
//			   triggerDetachMoveLeft(box);
//		   }
//	   } else if(box.rect.left >= c.rect.right) {
//		   if(!c.right.isEmpty()) {
//			   triggerDetachMoveRight(box);
//		   }
//	   }
//   }
//
//   private void triggerDetachMoveLeft(Box box) {
//	   Core c = MainActivity.core;
//
//	   Box NW = c.left.get(0);
//	   if(c.left.size() > 1) {
//		   NW = c.left.get(1);
//	   }
//
//	   int diff = (int) (box.maxY - NW.maxY);
//	   if(diff <= 0) {
//		   detMoveLeftUp.add(box);
//	   }
//
//	   NW = c.left.get(c.left.size()-1);
//
//	   diff = (int) (box.maxY - NW.maxY);
//	   if(diff > 0) {
//		   detMoveLeftDown.add(box);
//	   }
//   }

//   private void triggerDetachMoveRight(Box box) {
//	   Core c = MainActivity.core;
//
//	   Box NW = c.right.get(0);
//	   if(c.right.size() > 1) {
//		   NW = c.right.get(1);
//	   }
//
//	   int diff = (int) (box.maxY - NW.maxY);
//	   if(diff <= 0) {
//		   detMoveRightUp.add(box);
//	   }
//
//	   NW = c.right.get(c.right.size()-1);
//
//	   diff = (int) (box.maxY - NW.maxY);
//	   if(diff > 0) {
//		   detMoveRightDown.add(box);
//	   }
//   }

//   public void triggerLastDetachMove() {
//	   List<Box> det = MainActivity.core.detached;
//
//	   if(det.isEmpty()) {
//		   return;
//	   }
//
//	   Box b = det.get(det.size()-1);
//	   b.update();
//	   b.updateMaxY();
//	   b.updateMinY();
//	   triggerDetachMove(b);
//   }

    @Override
    public void onDraw(Canvas canvas) {
        this.canvas = canvas;
        //przesunięcie mapy i wyskalowanie
        canvas.translate(transx, transy);
        canvas.scale(zoomx, zoomy);
        Root root = MainActivity.root;
        drawBox(root, canvas);
        if (root == null) {
            return;
        }

        for (Box box : root.getLeftChildren()) {
            fireDrawChildren(box, canvas);
        }

        for (Box box : root.getRightChildren()) {
            fireDrawChildren(box, canvas);
        }
//       if(root.isExpanded()) {
//
//    	   //showLines(root);
//       } else {
//    	   //hideLines(root);
//       }
//
//       //jak jest włączony tryb przesuwania, to nic się nie zmienia, nie trzeba liczyć na nowo
        if (MainActivity.mActionMode != null && MainActivity.mActionMode.getTitle().toString().equalsIgnoreCase("move")) {
            //  paint.setStrokeWidth(0);
//
            drawBox(root, canvas);
//
            for (Box box : root.getLeftChildren()) {
                drawBox(box, canvas);
            }

            for (Box box : root.getRightChildren()) {
                drawBox(box, canvas);
            }

            for (Box box : root.getLeftChildren()) {
                fireDrawChildren(box, canvas);
            }

            for (Box box : root.getRightChildren()) {
                 fireDrawChildren(box, canvas);
            }

           for(Box box: root.getDetached()) {
        	   drawBox(box, canvas);
           }

           for(Box box: root.getDetached()) {
        	  fireDrawChildren(box, canvas);
           }

                return;
        }

       if(!root.getLeftChildren().isEmpty() && updateLeft) {
    	   for(Box box: root.getDetached()) {
    		  // triggerDetachMove(box);
    	   }
    	   updateLeft = false;
       }
//
//       if(!root.right.isEmpty() && updateRight) {
//    	   for(Box box: root.detached) {
//    		 //  triggerDetachMove(box);
//    	   }
//    	   updateRight = false;
//       }
//
//       for(Box box: root.left) {
//    	   if(box.isVisible()) {
//    		   //updateBox(box);
//    		   box.updateMaxY();
//    		   box.updateMinY();
//
//    		   if(box.isExpanded()) {
//    			 //  showLines(box);
//    		   } else {
//    			   //hideLines(box);
//    		   }
//    	   } else {
//    		  // hideLines(box);
//    	   }
//       }
//
//       for(Box box: root.right) {
//    	   if(box.isVisible()) {
//    		  // updateBox(box);
//    		   box.updateMaxY();
//    		   box.updateMinY();
//
//    		   if(box.isExpanded()) {
//    			  // showLines(box);
//    		   } else {
//    			   //hideLines(box);
//    		   }
//    	   } else {
//    		  // hideLines(box);
//    	   }
//       }
//
//       for(Box box: root.right) {
//    	  // firePrepareChildren(box);
//       }
//
//       for(Box box: root.left) {
//    	 //  firePrepareChildren(box);
//       }
//
//       for(Box box: root.detached) {
//		  // updateBox(box);
//		   box.updateMaxY();
//		   box.updateMinY();
//
//		   if(box.isExpanded()) {
//			//   showLines(box);
//		   } else {
//			 //  hideLines(box);
//		   }
//       }
//
//       for(Box box: root.detached) {
//    	   //firePrepareChildren(box);
//       }
//
//       if(revalidate) {
//    	   revalidate = false;
//    	   if(!root.left.isEmpty()) {
//    		   setOffsets(root.left, Position.LEFT, root);
//    	   }
//
//    	   if(!root.right.isEmpty()) {
//    		   setOffsets(root.right, Position.RIGHT, root);
//    	   }
//
//    	   for(Box box: root.left) {
//    		   setOffsets(box.position, box);
//    	   }
//
//    	   for(Box box: root.right) {
//    		   setOffsets(box.position, box);
//    	   }
//
//    	   for(Box box: root.detached) {
//    		   setOffsets(box.position, box);
//    	   }
//
//    	   for(Box box: root.left) {
//    		   adjustToChildren(box);
//    	   }
//
//    	   for(Box box: root.right) {
//    		   adjustToChildren(box);
//    	   }
//
//    	   for(Box box: root.detached) {
//    		   adjustToChildren(box);
//    	   }
//
//    	   //próba wymuszenia odstępów na bloczkach swobodnych
//
//    	   int maxLeft = 10000, maxRight = 10000;
//    	   List<Box> temp = new ArrayList<Box>();
//    	   temp.addAll(root.left);
//
//    	   while(!temp.isEmpty()) {
//    		   Box box = temp.get(0);
//    		   if(box.getChildren() != null && !box.getChildren().isEmpty()) {
//    			   temp.addAll(box.getChildren());
//    		   } else {
//    			   if(box.rect.left < maxLeft) {
//    				   maxLeft = box.rect.left;
//    				   maxRight = box.rect.right;
//    			   }
//    		   }
//
//    		   temp.remove(0);
//    	   }
//
//    	   if(!root.left.isEmpty()) {
//    		   Box NW = root.left.get(0);
//    		   int maxDiff = -10;
//
//    		   for(Box box: detMoveLeftUp) {
//    			   int diff = (int)(box.minY - NW.maxY);
//    			   if(diff > maxDiff) {
//    				   maxDiff = diff;
//    			   }
//    		   }
//
//    		   for(Box box: detMoveLeftUp) {
//    			   Utils.moveChildY(box, -maxDiff -20);
//    			   if(maxLeft < box.rect.right && box.rect.right < maxRight) {
//    				   Utils.moveChildX(box, (maxLeft - box.rect.right - 20));
//    			   }
//    		   }
//
//    		   NW = root.left.get(root.left.size()-1);
//    		   maxDiff = -10;
//
//    		   for(Box box: detMoveLeftDown) {
//    			   int diff = (int)(NW.minY - box.maxY);
//    			   if(diff > maxDiff) {
//    				   maxDiff = diff;
//    			   }
//    		   }
//
//    		   for(Box box: detMoveLeftDown) {
//    			   Utils.moveChildY(box, maxDiff + 20);
//    			   if(maxLeft < box.rect.right && box.rect.right < maxRight) {
//    				   Utils.moveChildX(box, (maxLeft - box.rect.right - 20));
//    			   }
//    		   }
//    	   }
//
//    	   if(!root.right.isEmpty()) {
//    		   Box NW = root.right.get(0);
//    		   int maxDiff = -10;
//
//    		   for(Box box: detMoveRightUp) {
//    			   int diff = (int)(box.minY - NW.maxY);
//    			   if(diff > maxDiff) {
//    				   maxDiff = diff;
//    			   }
//    		   }
//
//    		   for(Box box: detMoveRightUp) {
//    			   Utils.moveChildY(box, -maxDiff -20);
//    		   }
//
//    		   NW = root.right.get(root.right.size()-1);
//    		   maxDiff = -10;
//
//    		   for(Box box: detMoveRightDown) {
//    			   int diff = (int)(NW.minY - box.maxY);
//    			   if(diff > maxDiff) {
//    				   maxDiff = diff;
//    			   }
//    		   }
//
//    		   for(Box box: detMoveRightDown) {
//    			   Utils.moveChildY(box, maxDiff + 20);
//    		   }
//    	   }
//       }
//
//
//       detMoveLeftUp.clear();
//       detMoveLeftDown.clear();
//       detMoveRightDown.clear();
//       detMoveRightUp.clear();
//
//       paint.setStrokeWidth(5);
//
//       for(Line x: lines) {
//    	   if(x.isVisible()) {
//    		   Log.i("line", x.toString());
//
////    		   if(x.getColor() != 0) {
////    			   paint.setColor(x.getColor());
////    		   } else {
////    			   paint.setColor(Color.BLACK);
////    		   }
//
//    		   //canvas.drawLine(x.getStartX(), x.getStartY(), x.getEndX(), x.getEndY(), paint);
//    	   }
//       }
//
//       paint.setStrokeWidth(0);
//
        //drawBox(root, canvas);
//
//       for(Box box: root.left) {
//    	   drawBox(box, canvas);
//       }
//
//       for(Box box: root.right) {
//    	   drawBox(box, canvas);
//       }
//
//       for(Box box: root.left) {
//    	   //fireDrawChildren(box, canvas);
//       }
//
//       for(Box box: root.right) {
//    	  // fireDrawChildren(box, canvas);
//       }
//
//       for(Box box: root.detached) {
//    	   drawBox(box, canvas);
//       }
//
//       for(Box box: root.detached) {
//    	  // fireDrawChildren(box, canvas);
//       }

    }

    private float getLongest(String[] array) {
        float i = 0;

        for (String k : array) {
            if (paint.measureText(k) > i) {
                i = paint.measureText(k);
            }
        }
        return i;
    }

    private void fireDrawChildren(Box box, Canvas canvas) {
        drawBox(box, canvas);
    	for(Box child: box.getChildren()) {
    		drawBox(child, canvas);
    		fireDrawChildren(child, canvas);
    	}
    }

//    private void firePrepareChildren(Box box) {
//    	for(Box child: box.getChildren()) {
//    		if(child.isVisible()) {
//    			//updateBox(child);
//
//    			if(box.isExpanded()) {
//    				//showLines(box);
//    			} else {
//    				//hideLines(box);
//    			}
//    		} else {
//    			//hideLines(box);
//    		}
//    		firePrepareChildren(child);
//    	}
//    }

    private void setOffsets(Point p1, Point p2, Box parent) {
    	if(!parent.getChildren().isEmpty()) {
    		setOffsets(parent.getChildren(), p1, p2, parent);
    		for(Box box: parent.getChildren()) {
    			setOffsets(position, box);
    		}
    	}
    }

    //wyjustowanie względem rodzica
    private void setOffsets(List<Box> list, Point p1, Point p2, Box parent) {
    	Box first = list.get(0);
    	int diff = first.getDrawableShape().getBounds().top - parent.getDrawableShape().getBounds().top;
    	first.getDrawableShape().getBounds().top -= diff;
    	first.getDrawableShape().getBounds().bottom -= diff;

    	for(int x = 1; x < list.size(); x++) {
    		first = list.get(x);
    		diff = first.getDrawableShape().getBounds().top - (list.get(x-1).getDrawableShape().getBounds().bottom + 20);
    		first.getDrawableShape().getBounds().top -= diff;
    		first.getDrawableShape().getBounds().bottom -= diff;
    	}

    	int offset = 0;
    	int size = list.size();

    	first = list.get(size/2);
    	offset = first.getDrawableShape().getBounds().top - (parent.getDrawableShape().getBounds().top + parent.getDrawableShape().getBounds().bottom)/2;

    	for(Box b: list) {
    		b.getDrawableShape().getBounds().top -= offset;
    		b.getDrawableShape().getBounds().bottom -= offset;
    		updateBox(b);
    	}
    }

    //wyrównanie do dzieci
    private void adjustToChildren(Box box) {
    	Box temp = box;
    //	temp.updateMaxY();
    //	temp.updateMinY();

    	//dojście do najwyższego dziecka
    	while(!temp.getChildren().isEmpty()) {
    		temp = temp.getChildren().get(0);
    	}

    	while(temp.parent != null) {
    		temp.updateMaxY();
    		temp.updateMinY();
	    	//sprawdzamy rodzenstwo i odsuwamy je, jeśli jeszcze nie zostało odsunięte
	    	List<Box> siblings = temp.parent.getChildren();
	    	if(temp.parent instanceof Core) {
	    		if(temp.position == Position.LEFT) {
	    			siblings = ((Core) temp.parent).left;
	    		} else {
	    			siblings = ((Core) temp.parent).right;
	    		}
	    	}
	    	//od 1 bo temp to pierwsze dziecko

	    	for(int i = 1; i < siblings.size(); i++) {
    			siblings.get(i).updateMaxY();
    			siblings.get(i).updateMinY();
	    		int sibDiff = (int) (siblings.get(i-1).minY - siblings.get(i).maxY);
	    		if(sibDiff >= 0) {
	    			int diff = (int) (siblings.get(i).maxY - siblings.get(i-1).minY);
	    			Utils.moveChildY(siblings.get(i), -diff + 20);
	    			siblings.get(i).updateMaxY();
	    			siblings.get(i).updateMinY();
	    		}
	    	}
	    	temp = temp.parent;
    	}

    	for(Box child: box.getChildren()) {
    		adjustToChildren(child);
    	}
    }

    private void drawBox(Box box, Canvas canvas) {
        if (box.isVisible()) {
            //String s = box.getText().getText();
            //String[] parts = s.split("\n");
            //1float f = getLongest(parts);
            //
            if (box.getShape() != BlockShape.DIAMOND) {
                ((GradientDrawable) box.getDrawableShape()).setStroke((int) box.getLineThickness().getValue(), box.getLineColor());
            } else {
                ((GradientDrawable) ((RotateDrawable) box.getDrawableShape()).getDrawable()).setStroke((int) box.getLineThickness().getValue(), box.getLineColor());
            }

            // paint.setStrokeWidth(0);

         //   if (box instanceof Root) {
                //   box.setHeight(box.getDrawableShape().getBounds().top + parts.length * 30 + 10);
                //   box.setWidth(box.getDrawableShape().getBounds().left + (int)f + 50);
//                Rect rect = null;
//                float start = 0.5f * box.getDrawableShape().getBounds().height() / (parts.length);
//                Log.w("app",Float.toString(paint.measureText(box.getText().getText())));
//                box.setWidth(box.getWidth() + (int)(paint.measureText(box.getText().getText())));
                //paint.getTextBounds(box.getText().getText(), width - 10,height - 10, box.getDrawableShape().getBounds());
                box.prepareDrawableShape();
         //   }

            if (box.isSelected()) {
                box.setActiveColor();
            }// else if (!box.isExpanded()) {
//                paint.setColor(collapsedColor);
//            }
                box.getDrawableShape().draw(canvas);

            drawText(box, canvas);
            // todo sytuacja dla ciemnego tla
            box.newNote = context.getResources().getDrawable(R.drawable.ic_action_view_as_list);
            box.newNote.setBounds(box.getDrawableShape().getBounds().left, box.getDrawableShape().getBounds().top - 5 - ((BitmapDrawable) box.newNote).getBitmap().getHeight(),
                    box.getDrawableShape().getBounds().left + ((BitmapDrawable) box.newNote).getBitmap().getWidth(), box.getDrawableShape().getBounds().top - 5);
            box.newNote.draw(canvas);
            box.editBox = context.getResources().getDrawable(R.drawable.ic_action_edit);
            box.editBox.setBounds(box.getDrawableShape().getBounds().left + 5 + ((BitmapDrawable) box.newNote).getBitmap().getWidth(), box.getDrawableShape().getBounds().top - 5 - ((BitmapDrawable) box.editBox).getBitmap().getHeight(),
                    box.getDrawableShape().getBounds().left + 5 + ((BitmapDrawable) box.editBox).getBitmap().getWidth() + ((BitmapDrawable) box.newNote).getBitmap().getWidth(), box.getDrawableShape().getBounds().top - 5);
            box.editBox.draw(canvas);
            box.newMarker = context.getResources().getDrawable(R.drawable.ic_action_new_picture);
            box.newMarker.setBounds(box.getDrawableShape().getBounds().left + 5 + ((BitmapDrawable) box.editBox).getBitmap().getWidth() + ((BitmapDrawable) box.newNote).getBitmap().getWidth() + 5, box.getDrawableShape().getBounds().top - 5 - ((BitmapDrawable) box.newMarker).getBitmap().getHeight(),
                    box.getDrawableShape().getBounds().left + 5 + ((BitmapDrawable) box.editBox).getBitmap().getWidth() + ((BitmapDrawable) box.newNote).getBitmap().getWidth() + ((BitmapDrawable) box.newMarker).getBitmap().getWidth(), box.getDrawableShape().getBounds().top - 5);
            box.newMarker.draw(canvas);
            Rect rect = new Rect();
            rect.set(box.getDrawableShape().getBounds().left, box.getDrawableShape().getBounds().top - 5 - ((BitmapDrawable) box.newNote).getBitmap().getHeight(),
                    box.getDrawableShape().getBounds().left + ((BitmapDrawable) box.newNote).getBitmap().getWidth(), box.getDrawableShape().getBounds().top - 5);


//            for (int j = 0; j < parts.length; j++) {
//                paint.setColor(Color.BLACK);
//                String str = parts[j];
//                float start = 0.5f * box.getDrawableShape().getBounds().height() / (parts.length);
//                if (j == 0) {
//                     canvas.drawText(str, (box.getDrawableShape().getBounds().left + (box.getDrawableShape().getBounds().width() - f)/2), box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height())/(parts.length) * (j) + start + paint.getTextSize()/2, paint);
//                } else {
//                    canvas.drawText(str, (box.getDrawableShape().getBounds().left + (box.getDrawableShape().getBounds().width() - f)/2), box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height())/(parts.length) * (j) + start + paint.getTextSize()/2, paint);
//                }
//            }
        }
    }

    // ToDO wyswietlanie tekstu
    private void drawText(Box box, Canvas canvas) {
        if (box != null) {
            float x = 0;
            float y = 0;
            String s = box.getText().getText();
            String[] parts = s.split("\n");
            float f = getLongest(parts);
            if (box.getShape() != BlockShape.NO_BORDER || box.getShape() != BlockShape.UNDERLINE) {
                paint.setColor(box.getText().getColor().getColor());
            }
            paint.setTextSize(box.getText().getSize());
            if (box.getText().getAlign() == Align.CENTER) {
                paint.setTextAlign(Paint.Align.CENTER);
            } else if (box.getText().getAlign() == Align.RIGHT) {
                paint.setTextAlign(Paint.Align.RIGHT);
            } else if (box.getText().getAlign() == Align.LEFT) {
                paint.setTextAlign(Paint.Align.LEFT);
            }
            Rect rect = new Rect();
            paint.getTextBounds(s, 0, s.length(), rect);
            Log.w(s, rect.toString());
            Log.w("measute", String.valueOf(paint.measureText(s)));
            if (box.getText().isItalic() && box.getText().isBold()) {
                //todo dodac
                Typeface tf = Typeface.create("zainsalowana czcionka", Typeface.BOLD_ITALIC);
                paint.setTypeface(tf);
                // paint.setFakeBoldText(true);
            } else if (box.getText().isItalic()) {
                //todo dodac
                Typeface tf = Typeface.create("zainsalowana czcionka", Typeface.ITALIC);
                paint.setTypeface(tf);
            } else if (box.getText().isBold()) {
                //todo dodac
                Typeface tf = Typeface.create("zainsalowana czcionka", Typeface.BOLD);
                paint.setTypeface(tf);
            } else if (box.getText().isStrikeOut()) {
                paint.setStrokeWidth(2);
            } else {
                Typeface tf = Typeface.create("zainsalowana czcionka", Typeface.NORMAL);
                paint.setTypeface(tf);
            }

            if (box.getText().getAlign() == Align.CENTER) {
                paint.setTextAlign(Paint.Align.CENTER);
            } else if (box.getText().getAlign() == Align.RIGHT) {
                paint.setTextAlign(Paint.Align.RIGHT);
            } else if (box.getText().getAlign() == Align.LEFT) {
                paint.setTextAlign(Paint.Align.LEFT);
            }
            for (int j = 0; j < parts.length; j++) {
                String str = parts[j];
                float start = 0.4f * box.getDrawableShape().getBounds().height() / (parts.length);
                if (parts.length == 1) {
//               Rect rectText = new Rect();
//               paint.getTextBounds(box.getText().getText(), 0, box.getText().getText().length(), rectText);
//               Log.w("f", Float.toString(f));
//               Log.w("x", Integer.toString(box.getPoint().x));
//               Log.w("left", Integer.toString(box.getDrawableShape().getBounds().left));
//               Log.w("width", Integer.toString(box.getWidth()));
//               Log.w("x-cor", Float.toString((box.getDrawableShape().getBounds().left + ((box.getWidth() - f)/2))));
                    if (box.getText().getAlign() == Align.CENTER) {
                        if (box.getShape() == BlockShape.DIAMOND) {
                            x = box.getDrawableShape().getBounds().left + box.getWidth() / 2;
                            y = box.getDrawableShape().getBounds().top + (box.getWidth() / 2);
                        } else if (box.getShape() == BlockShape.ELLIPSE) {
                            x = box.getDrawableShape().getBounds().left + box.getWidth() / 2;
                            y = box.getDrawableShape().getBounds().top + (box.getHeight() / 2);
                        } else {
                            x = box.getDrawableShape().getBounds().left + box.getWidth() / 2;
                            y = box.getDrawableShape().getBounds().top + (box.getHeight() / parts.length) / 2;
                        }
                    } else if (box.getText().getAlign() == Align.RIGHT) {
                        if (box.getShape() == BlockShape.DIAMOND) {
                            x = (box.getDrawableShape().getBounds().right - (box.getWidth() - f) / 2);
                            y = box.getDrawableShape().getBounds().top + (box.getWidth() / 2);
                        } else if (box.getShape() == BlockShape.ELLIPSE) {
                            x = (box.getDrawableShape().getBounds().right - (box.getWidth() - f) / 2);
                            y = box.getDrawableShape().getBounds().top + (box.getHeight() / parts.length);
                        } else {
                            x = (box.getDrawableShape().getBounds().right - (box.getWidth() - f) / 2);
                            y = box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height()) / (parts.length) * (j) + start + paint.getTextSize() / 2;
                        }
                    } else if (box.getText().getAlign() == Align.LEFT) {
                        if (box.getShape() == BlockShape.DIAMOND) {
                            x = (box.getDrawableShape().getBounds().left + (box.getWidth() - f) / 2);
                            y = box.getDrawableShape().getBounds().top + (box.getWidth() / 2);
                        } else if (box.getShape() == BlockShape.ELLIPSE) {
                            x = (box.getDrawableShape().getBounds().left + (box.getWidth() - f) / 2);
                            y = box.getDrawableShape().getBounds().top + (box.getHeight() / parts.length);
                        } else {
                            x = (box.getDrawableShape().getBounds().left + (box.getWidth() - f) / 2);
                            y = box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height()) / (parts.length) * (j) + start + paint.getTextSize() / 2;
                        }

                    }
                    canvas.drawText(str, x, y, paint);
                    //canvas.drawText(str, (box.getDrawableShape().getBounds().left + (box.getWidth()- f)/2), box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height())/(parts.length) * (j) + start + paint.getTextSize()/2, paint);
                } else {
                    if (box.getText().getAlign() == Align.CENTER) {
                        if (box.getShape() == BlockShape.DIAMOND) {
                            //TODO poprawic
                            x = box.getDrawableShape().getBounds().left + box.getWidth() / 2;
                            y = box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height()) / (parts.length) * (j) + start + paint.getTextSize() / 2;
                        } else if (box.getShape() == BlockShape.ELLIPSE) {
                            start = 0.2f * box.getDrawableShape().getBounds().height() / (parts.length);
                            x = box.getDrawableShape().getBounds().left + box.getWidth() / 2;
                            y = box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height()) / (parts.length) * (j) + start + paint.getTextSize();
                        } else {
                            x = box.getDrawableShape().getBounds().left + box.getWidth() / 2;
                            y = box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height()) / (parts.length) * (j) * paint.getTextSize();
                        }
                    } else if (box.getText().getAlign() == Align.RIGHT) {
                        if (box.getShape() == BlockShape.DIAMOND) {
                            //TODO poprawic
                            x = (box.getDrawableShape().getBounds().right - (box.getWidth() - f) / 2);
                            y = box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height()) / (parts.length) * (j) + start + paint.getTextSize() / 2;
                        } else if (box.getShape() == BlockShape.ELLIPSE) {
                            start = 0.2f * box.getDrawableShape().getBounds().height() / (parts.length);
                            x = (box.getDrawableShape().getBounds().right - (box.getWidth() - f) / 2);
                            y = box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height()) / (parts.length) * (j) + start + paint.getTextSize();
                        } else {
                            x = (box.getDrawableShape().getBounds().right - (box.getWidth() - f) / 2);
                            y = box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height()) / (parts.length) * (j) + start + paint.getTextSize() / 2;
                        }

                    } else if (box.getText().getAlign() == Align.LEFT) {
                        if (box.getShape() == BlockShape.DIAMOND) {
                            //TODO poprawic
                            x = (box.getDrawableShape().getBounds().left + (box.getWidth() - f) / 2);
                            y = box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height()) / (parts.length) * (j) + start + paint.getTextSize() / 2;
                        } else if (box.getShape() == BlockShape.ELLIPSE) {
                            start = 0.2f * box.getDrawableShape().getBounds().height() / (parts.length);
                            x = (box.getDrawableShape().getBounds().left + (box.getWidth() - f) / 2);
                            y = box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height()) / (parts.length) * (j) + start + paint.getTextSize();
                        } else {
                            x = (box.getDrawableShape().getBounds().left + (box.getWidth() - f) / 2);
                            y = box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height()) / (parts.length) * (j) + start + paint.getTextSize() / 2;
                        }
                        //x = (box.getDrawableShape().getBounds().left + (box.getWidth()- f)/2);
                        //y = box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height())/(parts.length) * (j) + start + paint.getTextSize()/2;
                    }
                    canvas.drawText(str, x, y, paint);
                    //canvas.drawText(str, x, y * (j) + start + paint.getTextSize()/2, paint);
                    //canvas.drawText(str, (box.getDrawableShape().getBounds().left + (box.getDrawableShape().getBounds().width() - f)/2), box.getDrawableShape().getBounds().top + (box.getDrawableShape().getBounds().height())/(parts.length) * (j) + start + paint.getTextSize()/2, paint);
                }
            }
        }
    }


    public void updateBox(Box box) {
        String s = box.getText().getText();
        String[] parts = s.split("\n");

        float f = getLongest(parts);
        box.getDrawableShape().setBounds(box.getDrawableShape().getBounds().left, box.getDrawableShape().getBounds().top,
             box.getDrawableShape().getBounds().right, box.getDrawableShape().getBounds().top + parts.length * box.getText().getSize() + 10);
        box.getDrawableShape().getBounds().bottom = box.getDrawableShape().getBounds().top + parts.length * 30 + 10;
        if (box instanceof Root) {
            box.setWidth((int) f + 30);
              box.getDrawableShape().setBounds(box.getDrawableShape().getBounds().left, box.getDrawableShape().getBounds().top,
                     box.getDrawableShape().getBounds().right + (int) f + 50, box.getDrawableShape().getBounds().bottom);
              box.getDrawableShape().getBounds().right = box.getDrawableShape().getBounds().left + (int) f + 50;
        }
//    	else if(box.xmlPosition != null) {
//    		box.rect.right = box.rect.left + (int) f + 50;
//    		box.xmlPosition.x = box.rect.left;
//    		box.xmlPosition.y = box.rect.top;
//    	}
        //else if(box.position == Position.RIGHT) {
        else {
            //TODO sprawdzic i poprawic
            box.getPoint().x = box.getParent().getDrawableShape().getBounds().right + 20;
            box.setWidth((int) f + 50);
        }
//    	} else {
//    		box.rect.right = box.parent.rect.left - 20;
//        	box.rect.left = box.rect.right - ((int) f + 50);
//    	}
        if (parts.length != 1) {
            if (box.getShape() == BlockShape.ELLIPSE) {
                box.setWidth((int) (f * 1.5));
                box.setHeight(parts.length * (box.getText().getSize() + 40));
            } else {
                box.setHeight(parts.length * (box.getText().getSize() + 10));
            }
        }
          drawText(box, canvas);
    }

//    //dodanie linii
//    public void addLine(Box b1, Box b2) {
//    	//Line newLine = new Line(b1, b2);
//
//    	//if(!lines.contains(newLine)) {
//    		//newLine.setColor(b2.color);
//    		//lines.add(newLine);
//    	}
//    }
//
//    //usunięcie linii
//    public void deleteLine(Box b1, Box b2) {
//    	Line newLine = new Line(b1, b2);
//    	if(lines.contains(newLine)) {
//    		lines.remove(newLine);
//    	} else {
//    		newLine = new Line(b2, b1);
//    		if(lines.contains(newLine)) {
//        		lines.remove(newLine);
//        	}
//    	}
//    }

    //aktualizacja wyświetlania linii po przepięciu bloczka i propagacja koloru
    public void updateLineForChildren(Box parent) {
        for (int i = 0; i < lines.size(); i++) {
            Line x = lines.get(i);

            if (!x.isVisible()) {
                continue;
            }

//    		if(x.getEnd1().equals(parent)) {
//    			x.setColor(parent.color);
//    			for(Box child: parent.getChildren()) {
//    				child.color = parent.color;
//    				updateLineForChildren(child);
//    			}
//    		}
        }
    }

    public void updateCore(Root core) {
        String s = core.getText().getText();
        String[] parts = s.split("\n");

        float f = getLongest(parts);

        core.getDrawableShape().getBounds().bottom = core.getDrawableShape().getBounds().top + parts.length * 30 + 10;
        core.getDrawableShape().getBounds().right = core.getDrawableShape().getBounds().left + (int) f + 50;
    }

//    //usuwanie linii wychodzących z bloczka lub wchodzących do niego i usunięcie bloczka z listy dzieci rodzica
//    private void clear(Box b) {
//    	for(int i = 0; i < lines.size(); i++) {
//    		Line x = lines.get(i);
//    		if(x.getEnd1().equals(b) || x.getEnd2().equals(b)) {
//    			lines.remove(x);
//    			i--;
//    		}
//    	}
//
//    	b.parent.removeChild(b);
//    }
//
//    public void eraseHimAndChildrenFromView(Box b) {
//    	List<Box> temp = b.getChildren();
//
//    	while(!temp.isEmpty()) {
//    		eraseHimAndChildrenFromView(temp.get(0));
//    	}
//
//    	clear(b);
//    }

//    public void deleteHimAndChildren(Box b) {
//    	eraseHimAndChildrenFromView(b);
//    	b.deleteBoxAndChildren();
//    }

    //    private void hideLines(Box b) {
//    	for(int i = 0; i < lines.size(); i++) {
//    		Line x = lines.get(i);
//
//    		if(!x.isVisible()) {
//    			continue;
//    		} else if(x.getEnd1().equals(b)) {
//    			x.setVisible(false);
//    		}
//    	}
//    }
//
//    private void showLines(Box b) {
//    	for(int i = 0; i < lines.size(); i++) {
//    		Line x = lines.get(i);
//
//    		if(x.isVisible()) {
//    			continue;
//    		} else if(x.getEnd1().equals(b)) {
//    			x.setVisible(true);
//    		}
//    	}
//    }
    private class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        public EndlessScrollListener() {
        }

        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // I load the next page of gigs using a background task,
                // but you can call any function here.
                //new LoadGigsTask().execute(currentPage + 1);
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

    }

    public void updateText() {
        drawText(MainActivity.boxEdited, canvas);
    }


//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        int x = (int) event.getX();
//        int y = (int) event.getY();
//
//        Box box = Utils.whichBox(this, event);
//        switch(event.getAction()){
//            case MotionEvent.ACTION_DOWN:
//
//
//                    Log.w("action", "down");
////                for(int i=0; i < rectangles.size(); i++){
////                    View child = rectangles.get(i);
////                    if(x > child.getLeft() && x < child.getLeft()+150 && y > child.getTop() && y < child.getTop()+50){
////                        rectId = child.getId();
////                        rectangles.get(rectId-1).setTop(child.getTop());
////                        rectangles.get(rectId-1).setLeft(child.getLeft());
////                        break;
////                    }
////                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                Log.w("action", "move");
////                if(rectId > 0){
////                    rectangles.get(rectId-1).setBackgroundRessource(true);
////                    rectangles.get(rectId-1).setTop(y-25);
////                    rectangles.get(rectId-1).setLeft(x-25);
////                    Log.i("MOVE X", rectId+" "+rectangles.get(rectId-1).getX());
////                    Log.i("MOVE Y", rectId+" "+rectangles.get(rectId-1).getY());
////                }
//
//                break;
//            case MotionEvent.ACTION_UP:
//                Log.w("action", "up");
////                Log.i("rectid Cancel", ""+rectId);
////                if(rectId > 0){
////                    rectangles.get(rectId-1).setBackgroundRessource(true);
////                }
//
//                break;
//        }
//
//        invalidate();
//        return true;
//
//    }
}