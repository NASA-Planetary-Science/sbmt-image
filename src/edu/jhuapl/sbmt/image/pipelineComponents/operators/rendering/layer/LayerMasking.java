package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer;

public class LayerMasking
{
	int left;
	int right;
	int top;
	int bottom;

	public LayerMasking(int left, int right, int top, int bottom)
	{
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}

	public LayerMasking(int[] masking)
	{
		this.left = masking[0];
		this.right = masking[1];
		this.top = masking[2];
		this.bottom = masking[3];
	}

	public int[] getMask()
	{
		return new int[] {left, right, top, bottom};
	}
}
